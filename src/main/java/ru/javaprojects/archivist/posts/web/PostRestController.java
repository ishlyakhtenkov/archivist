package ru.javaprojects.archivist.posts.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.posts.PostService;

import static ru.javaprojects.archivist.posts.web.PostUIController.POSTS_URL;

@RestController
@RequestMapping(value = POSTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class PostRestController {
    private final PostService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete post with id={}", id);
        service.delete(id);
    }
}
