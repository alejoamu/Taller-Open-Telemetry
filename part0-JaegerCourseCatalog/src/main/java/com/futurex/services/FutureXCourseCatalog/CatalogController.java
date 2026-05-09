package com.futurex.services.FutureXCourseCatalog;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CatalogController {

    @Value("${course.service.url}")
    private String courseServiceUrl;

    private final RestTemplate restTemplate;

    @Autowired
    private Tracer tracer;

    public CatalogController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @RequestMapping("/")
    public String getCatalogHome() {
        Span span = tracer.spanBuilder("getCatalogHome").startSpan();
        try {
            String courseAppMessage = restTemplate.getForObject(courseServiceUrl, String.class);
            return "Welcome to FutureX Course Catalog " + courseAppMessage;
        } finally {
            span.end();
        }
    }

    @RequestMapping("/catalog")
    public String getCatalog() {
        Span span = tracer.spanBuilder("getCatalog").startSpan();
        try {
            String courses = restTemplate.getForObject(courseServiceUrl + "/courses", String.class);
            return "Our courses are " + courses;
        } finally {
            span.end();
        }
    }

    @RequestMapping("/firstcourse")
    public String getSpecificCourse() {
        Span span = tracer.spanBuilder("getSpecificCourse").startSpan();
        try {
            Course course = restTemplate.getForObject(courseServiceUrl + "/1", Course.class);
            return "Our first course is " + course.getCoursename();
        } finally {
            span.end();
        }
    }
}