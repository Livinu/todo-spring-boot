package com.manning.gia.todo.web;

import com.manning.gia.todo.model.ToDoItem;
import com.manning.gia.todo.repository.ToDoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ToDoController {
    public static final String INDEX_PAGE = "todo-list";
    public static final String ITEMS = "toDoItems";
    public static final String STATS = "stats";
    public static final String FILTER = "filter";
    public static final String REDIRECT = "redirect";
    
    

    private ToDoRepository toDoRepository;

    @Autowired
    public ToDoController(ToDoRepository toDoRepository) {
        this.toDoRepository = toDoRepository;
    }

    @RequestMapping("/")
    public String index(){
        return "redirect:/all";
    }

    @GetMapping(value = "/all")
    public String allItems(Model model) {
        List<ToDoItem> toDoItems = toDoRepository.findAll();
        model.addAttribute(ITEMS, toDoItems);
        model.addAttribute(STATS, determineStats(toDoItems));
        model.addAttribute(FILTER, "all");
        return INDEX_PAGE;
    }

    @GetMapping(value = "/active")
    public String activeItems(Model model) {
        List<ToDoItem> toDoItems = toDoRepository.findAll();
        model.addAttribute(ITEMS, filterBasedOnStatus(toDoItems, true));
        model.addAttribute(STATS, determineStats(toDoItems));
        model.addAttribute(FILTER, "active");
        return INDEX_PAGE;
    }

    @GetMapping(value = "/completed")
    public String completedItems(Model model) {
        List<ToDoItem> toDoItems = toDoRepository.findAll();
        model.addAttribute(ITEMS, filterBasedOnStatus(toDoItems, false));
        model.addAttribute(STATS, determineStats(toDoItems));
        model.addAttribute(FILTER, "active");
        return INDEX_PAGE;
    }

    @PostMapping(value = "/insert")
    public String insertItem(@RequestParam String name, @RequestParam String filter) {
        ToDoItem toDoItem = new ToDoItem();
        toDoItem.setName(name);
        toDoRepository.save(toDoItem);
        return  REDIRECT+ ":" + filter;
    }

    @PostMapping(value = "/update")
    public String updateItem(@RequestParam Long id, @RequestParam String name, @RequestParam String filter) {
        ToDoItem toDoItem = toDoRepository.findOne(id);

        if(toDoItem != null) {
            toDoItem.setName(name);
            toDoRepository.save(toDoItem);
        }

        return REDIRECT+ ":" + filter;
    }

    @PostMapping(value = "/delete")
    public String deleteItem(@RequestParam Long id, @RequestParam String filter) {
        ToDoItem toDoItem = toDoRepository.findOne(id);

        if(toDoItem != null) {
            toDoRepository.delete(toDoItem);
        }

        return REDIRECT+ ":" + filter;
    }

    @PostMapping(value = "/toggleStatus")
    public String toggleStatus(@RequestParam Long id, @RequestParam(required = false) Boolean toggle, @RequestParam String filter) {
        ToDoItem toDoItem = toDoRepository.findOne(id);

        if(toDoItem != null) {
            boolean completed = (toggle == null || toggle == Boolean.FALSE) ? false : true;
            toDoItem.setCompleted(completed);
            toDoRepository.save(toDoItem);
        }

        return REDIRECT+ ":" + filter;
    }

    @PostMapping(value = "/clearCompleted")
    public String clearCompleted(@RequestParam String filter) {
        List<ToDoItem> toDoItems = toDoRepository.findAll();

        for(ToDoItem toDoItem : toDoItems) {
            if(toDoItem.isCompleted()) {
                toDoRepository.delete(toDoItem);
            }
        }

        return REDIRECT+ ":" + filter;
    }

    private List<ToDoItem> filterBasedOnStatus(List<ToDoItem> toDoItems, boolean active) {
        List<ToDoItem> filteredToDoItems = new ArrayList<>();

        for(ToDoItem toDoItem : toDoItems) {
            if(toDoItem.isCompleted() != active) {
                filteredToDoItems.add(toDoItem);
            }
        }

        return filteredToDoItems;
    }


    private ToDoListStats determineStats(List<ToDoItem> toDoItems) {
        ToDoListStats toDoListStats = new ToDoListStats();

        for(ToDoItem toDoItem : toDoItems) {
            if(toDoItem.isCompleted()) {
                toDoListStats.addCompleted();
            }
            else {
                toDoListStats.addActive();
            }
        }

        return toDoListStats;
    }

    public static class ToDoListStats {
        private int active;
        private int completed;

        private void addActive() {
            active++;
        }

        private void addCompleted() {
            completed++;
        }

        public int getActive() {
            return active;
        }

        public int getCompleted() {
            return completed;
        }

        public int getAll() {
            return active + completed;
        }

        void setActive(int active) {
            this.active = active;
        }

        void setCompleted(int completed) {
            this.completed = completed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ToDoListStats stats = (ToDoListStats) o;

            if (active != stats.active) return false;
            return completed == stats.completed;
        }

        @Override
        public int hashCode() {
            int result = active;
            result = 31 * result + completed;
            return result;
        }
    }
}
