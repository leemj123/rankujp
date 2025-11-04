package com.lee.rankujp.core.sitemap;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SeoController {

    private final SitemapService sitemapService;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml")
    @ResponseBody
    public String sitemap() {return sitemapService.siteMapList();}

    @GetMapping(value = "/sitemap{page}.xml", produces = "application/xml")
    @ResponseBody
    public String sitemapPage(@PathVariable int page) {
        return sitemapService.makeSiteMap(page);
    }

//    @GetMapping(value = "/rss.xml", produces = "application/xml")
//    @ResponseBody
//    public String rssFeed() {return sitemapService.makeRss();}
}
