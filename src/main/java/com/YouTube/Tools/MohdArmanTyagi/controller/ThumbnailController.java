package com.YouTube.Tools.MohdArmanTyagi.controller;

import com.YouTube.Tools.MohdArmanTyagi.service.ThumbnailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ThumbnailController {
    @Autowired
    ThumbnailService service;
    @GetMapping("/thumbnails")
    public String getThumbnail()
    {
        return "thumbnails";
    }

    @PostMapping("/get-thumbnail")
    public  String showThumbnail(@RequestParam("videoUrlOrId") String videoUrlId, Model model)
    {
        String videoId = service.extractVideoId(videoUrlId);
        if(videoId == null)
        {
            model.addAttribute("error", "Invalid YouTube URL");
            return "thumbnails";
        }
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
        model.addAttribute("thumbnailUrl",thumbnailUrl);

        return  "thumbnails";

    }
}
