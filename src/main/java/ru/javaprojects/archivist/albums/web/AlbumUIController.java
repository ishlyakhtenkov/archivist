package ru.javaprojects.archivist.albums.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.albums.AlbumService;
import ru.javaprojects.archivist.albums.to.AlbumTo;

import static ru.javaprojects.archivist.albums.AlbumUtil.asTo;

@Controller
@RequestMapping(AlbumUIController.ALBUMS_URL)
@AllArgsConstructor
@Slf4j
public class AlbumUIController {
    static final String ALBUMS_URL = "/albums";

    private final AlbumService service;
    private final UniqueAlbumValidator albumValidator;

    @InitBinder("albumTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(albumValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<Album> albums;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/albums";
            }
            log.info("get albums (pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(),
                    pageable.getPageSize(), keyword);
            albums = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("get albums (pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            albums = service.getAll(pageable);
        }
        if (albums.getContent().isEmpty() && albums.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/albums";
        }
        model.addAttribute("albums", albums);
        return "albums/albums";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get album with id={}", id);
        model.addAttribute("album", service.getWithIssuances(id));
        return "albums/album";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show album add form");
        model.addAttribute("albumTo", new AlbumTo());
        model.addAttribute("stamps", Stamp.values());
        return "albums/album-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for album with id={}", id);
        Album album = service.getWithIssuances(id);
        model.addAttribute("albumTo", asTo(album));
        model.addAttribute("stamps", Stamp.values());
        model.addAttribute("album", album);
        return "albums/album-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid AlbumTo albumTo, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            if (!albumTo.isNew()) {
                model.addAttribute("album", service.getWithIssuances(albumTo.getId()));
            }
            model.addAttribute("stamps", Stamp.values());
            return "albums/album-form";
        }
        boolean isNew = albumTo.isNew();
        log.info((isNew ? "create" : "update") + " {}", albumTo);
        Long albumId = albumTo.getId();
        if (isNew) {
            Album created = service.create(albumTo);
            albumId = created.id();
        } else {
            service.update(albumTo);
        }
        redirectAttributes.addFlashAttribute("action", "Album " + albumTo.getDecimalNumber() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/albums/" + albumId;
    }
}
