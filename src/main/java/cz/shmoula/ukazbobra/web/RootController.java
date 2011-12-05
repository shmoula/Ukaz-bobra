package cz.shmoula.ukazbobra.web;

import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.format.DateTimeFormat;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import cz.shmoula.ukazbobra.domain.Image;

/**
 * Kontroler pro obsluhu souboru v rootu - index a tak
 * @author vbalak
 *
 */

@RequestMapping("/")
@Controller
public class RootController {
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	@RequestMapping(method = RequestMethod.GET)
	public String index(Model model) {
		
		model.addAttribute("image_url", getRandomImageUrl());
		
		model.addAttribute("image", new Image());
        addDateTimeFormatPatterns(model);
		
		return "index";
	}
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public String createdoc(@Valid Image image, BindingResult result, Model model, @RequestParam("content") MultipartFile content, HttpServletRequest request) {
		image.setContentType(content.getContentType());
		image.setFilename(content.getOriginalFilename());
		image.setFilesize(content.getSize());
		image.setUploaded(new Date());

		if (!result.hasErrors()) {
			image.persist();
		}
		
		model.addAttribute("image_url", getRandomImageUrl());
		
		model.addAttribute("image", image);
        addDateTimeFormatPatterns(model);

		return "index";
	}
	
	// TODO: tady je jenom int, pohlidat do budoucna meze
	private String getRandomImageUrl(){
		Random randomGenerator = new Random();
		long imagesCount = Image.countImages();
		
		if(imagesCount == 0)
			return "http://www.kuradomowa.com/dzieci/kolorowanki/bobr_2.jpg";
		
		int random = randomGenerator.nextInt((int)imagesCount);
		random += 1;
		
		return "images/show/" + random;
	}
	
	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("image_uploaded_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }
}
