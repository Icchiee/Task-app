package com.example.task_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.task_app.dto.request.TaskCreateRequest;
import com.example.task_app.service.TaskService;

import jakarta.validation.Valid;

@Controller
public class TaskController {

    private final TaskService taskService;
    @AuthenticationPrincipal UserDetails currentUser

    @GetMapping("/task/new")
    public String taskCreatePage(Model model){
        model.addAttribute("form", new TaskCreateRequest());
        return "task/new";
    }

    @GetMapping("/tasks")
    public String tasksListPage(){
        return "task/list";
    }

    @PostMapping("/tasks")
    public String create(@Valid @ModelAttribute("form") TaskCreateRequest request, BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()) {      
            return "task/new";
        }
        try{
            taskService.create(request, currentUser.getUsername());
            return "redirect:/tasks";
        }

    }
}
