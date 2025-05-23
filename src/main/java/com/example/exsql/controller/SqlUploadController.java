package com.example.exsql.controller;

import com.example.exsql.model.ScriptExecutionResult;
import com.example.exsql.service.SqlExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SqlUploadController {

    private static final Logger logger = LoggerFactory.getLogger(SqlUploadController.class);

    @Autowired
    private SqlExecutionService sqlExecutionService;

    @GetMapping("/")
    public String uploadPage(Model model) {
        // Ensure results attribute is present, even if empty, for Thymeleaf
        if (!model.containsAttribute("results")) {
            model.addAttribute("results", Collections.emptyList());
        }
        if (!model.containsAttribute("error")) {
            model.addAttribute("error", null);
        }
         if (!model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", null);
        }
        // Add data source names for the UI
        model.addAttribute("dataSourceNames", Arrays.asList("primary", "secondary"));
        return "upload";
    }

    @PostMapping("/upload")
    public String uploadAndExecuteSql(@RequestParam("files") MultipartFile[] files,
                                      @RequestParam(name = "dataSourceName", defaultValue = "primary") String dataSourceName,
                                      RedirectAttributes redirectAttributes, Model model) {
        if (files == null || files.length == 0 || Arrays.stream(files).allMatch(MultipartFile::isEmpty)) {
            redirectAttributes.addFlashAttribute("error", "Please select one or more SQL script files to upload.");
            return "redirect:/";
        }
        
        // Filter out empty files just in case, though the above check should mostly cover it.
        List<MultipartFile> nonEmptyFiles = Arrays.stream(files)
                                                .filter(file -> !file.isEmpty())
                                                .collect(Collectors.toList());

        if (nonEmptyFiles.isEmpty()) {
             redirectAttributes.addFlashAttribute("error", "All selected files were empty.");
            return "redirect:/";
        }

        try {
            logger.info("Received request to execute scripts on data source: {}", dataSourceName);
            List<ScriptExecutionResult> results = sqlExecutionService.executeSqlScripts(nonEmptyFiles.toArray(new MultipartFile[0]), dataSourceName);
            redirectAttributes.addFlashAttribute("results", results);
            boolean allSuccess = results.stream().allMatch(ScriptExecutionResult::isSuccess);
            if (allSuccess) {
                 redirectAttributes.addFlashAttribute("successMessage", "All scripts processed. See details below.");
            } else {
                 redirectAttributes.addFlashAttribute("error", "Some scripts failed to execute. See details below.");
            }
            logger.info("SQL execution results: {}", results);
        } catch (Exception e) {
            logger.error("Error during SQL script execution: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + e.getMessage());
        }
        return "redirect:/";
    }
} 