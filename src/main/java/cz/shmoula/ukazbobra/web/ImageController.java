package cz.shmoula.ukazbobra.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import cz.shmoula.ukazbobra.domain.Image;

import org.apache.commons.io.IOUtils;
import org.springframework.roo.addon.web.mvc.controller.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

@RooWebScaffold(path = "images", formBackingObject = Image.class)
@RequestMapping("/images")
@Controller
public class ImageController {

	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	public String createdoc(@Valid Image image, BindingResult result, Model model, @RequestParam("content") MultipartFile content, HttpServletRequest request) {
		image.setContentType(content.getContentType());
		image.setFilename(content.getOriginalFilename());
		image.setFilesize(content.getSize());
		image.setUploaded(new Date());

		if (result.hasErrors()) {
			model.addAttribute("image", image);
			return "images/create";
		}
		image.persist();

		return "redirect:/images?page=1&size=10" + encodeUrlPathSegment(image.getId().toString(), request);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String show(@PathVariable("id") Long id, Model model) {
		Image doc = Image.findImage(id);
		
		doc.setUrl("/images/show/" + id);
		model.addAttribute("image", Image.findImage(id));
		model.addAttribute("itemId", id);
		
		return "images/show";
	}

	@RequestMapping(value = "/show/{id}", method = RequestMethod.GET)
	public String showdoc(@PathVariable("id") Long id, HttpServletResponse response, Model model) {
		Image doc = Image.findImage(id);

		try {
			response.setHeader("Content-Disposition", "inline;filename=\"" + doc.getFilename() + "\"");

			OutputStream out = response.getOutputStream();
			response.setContentType(doc.getContentType());
			IOUtils.copy(new ByteArrayInputStream(doc.getContent()), out);
			out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
