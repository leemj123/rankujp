package com.lee.rankujp.core.sitemap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeoController {

    private final SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String sitemap() {return sitemapService.makeSiteMap();}

//    @GetMapping(value = "/rss.xml", produces = "application/xml")
//    @ResponseBody
//    public String rssFeed() {return sitemapService.makeRss();}
}
