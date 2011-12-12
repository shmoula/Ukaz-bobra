package cz.shmoula.ukazbobra.web;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

import cz.shmoula.ukazbobra.domain.Image;

/**
 * Kontroler pro obsluhu souboru v rootu - index a tak
 * @author vbalak
 *
 */

@RequestMapping("/")
@Controller
public class RootController {
	@Autowired
	private MessageSource messageSource;
	
	private static final int MAX_FILESIZE = 500000; // bajtu
	
	@InitBinder
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws ServletException {
		binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
	}

	@RequestMapping(method = RequestMethod.GET)
	public String index(Model model) {
		
		model.addAttribute("image_url", getRandomImageUrl());
		model.addAttribute("beavers_count", Image.countImages());
		
		model.addAttribute("image", new Image());
        addDateTimeFormatPatterns(model);
		
		return "index";
	}
	
	/**
	 * Nahrani noveho obrazku
	 * @param image
	 * @param result
	 * @param model
	 * @param content
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "save", method = RequestMethod.POST)
	public String createImage(@Valid Image image, BindingResult result, Model model, @RequestParam("content") MultipartFile content, HttpServletRequest request) {

		if (!result.hasErrors()) {
			image.setContentType(content.getContentType());
			image.setFilename(content.getOriginalFilename());
			image.setFilesize(content.getSize());
			image.setUploaded(new Date());
			
			// validace kontentu a velikosti
			if(!content.getContentType().contains("image/")) {
				model.addAttribute("error_message", messageSource.getMessage("string_error_not_image", null, Locale.ENGLISH));
			} else if (content.getSize() > MAX_FILESIZE) {
				model.addAttribute("error_message", messageSource.getMessage("string_error_big_file", null, Locale.ENGLISH));
			} else {
				image.persist();
				model.addAttribute("info_message", messageSource.getMessage("string_info_successfully_uploaded", null, Locale.ENGLISH));
			}
		} else
			model.addAttribute("error_message", messageSource.getMessage("string_error_validation_problem", null, Locale.ENGLISH));

		// primo zobrazim nahrany obrazek
		return show(image.getId(), model);
	}
	
	/**
	 * Zobrazeni konkretniho obrazku - kvuli sdileni a tak :-)
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String show(@PathVariable("id") Long id, Model model) {
		Image doc = Image.findImage(id);
		
		// existuje vubec tento obrazek?
		if(doc == null)
			model.addAttribute("error_message", messageSource.getMessage("string_error_not_exist", null, Locale.ENGLISH));
		else
			model.addAttribute("image_url", "images/show/" + id);
		
		return "show";
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
