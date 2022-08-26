package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response 
	@ModelAttribute
	public void addCommandata(Model model,Principal principal) {
		String username = principal.getName();
		System.out.println("USERNAME "+username);
		
		//get the user using username(Email)
		
		User user = userRepository.getUserByUserName(username);
		System.out.println("USER "+user);
		
		model.addAttribute("user",user);
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
		
	}
	
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_from";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,HttpSession session) 
	{
		try {
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		
//		//Checking Error Message
//		if (3>2) {
//			throw new Exception();
//		}
//		

		
		//processing and uploading file
		if (file.isEmpty()) {
			//if the file is empty then try our message
			System.out.println("File is empty..??");
			contact.setImage("contactLogo.png");
			
		}else {
			//upload the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/img").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded..!!");
		}
		
		user.getContacts().add(contact);
		
		//for bi-directional mapping
		contact.setUser(user);
		
		this.userRepository.save(user);
		
		System.out.println("DATA :"+contact);
		
		System.out.println("Added to Database");
		
		//message success....
		session.setAttribute("message", new Message("Your Contact is added !! Add more..", "success"));
		
		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR : "+e.getMessage());
			e.printStackTrace();
			
			//error message
			session.setAttribute("message", new Message("Something went wrong !! Try again", "danger"));
		}
		
		return "normal/add_contact_from";
	}
	
	//show contact handler
	//per page = 5 contact show
	//current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page")Integer page, Model m,Principal principal) {
		//title
		m.addAttribute("title","View User Contacts");
	
		//send contact list to page
		//first get contacts from Database
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		//after pagination
//		List<Contact> contacts = this.contactRepository.findContactsByUsers(user.getId());
		
		//current page-page
		//contact per page - 3
		PageRequest pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUsers(user.getId(),pageable);
		
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//showing specific contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		System.out.println("CID : "+cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get();
		
		//checking which user login
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		if (user.getId()==contact.getUser().getId()) {
			
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		System.out.println("CID : "+cId);
		
		Contact contact = contactOptional.get();
		
		contact.setUser(null);
		
		//user checking incomplete
		
		this.contactRepository.delete(contact);
		
		System.out.println("deleted..!!");
		session.setAttribute("message",new Message("Contact deleted successfully..!!" ,"success"));
		
		return "redirect:/user/show-contacts/0";
		
	}
	
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model m) {
		
		m.addAttribute("title","Update_contact");
		
		Contact contact= this.contactRepository.findById(cid).get();
		
		m.addAttribute("contact",contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value = "process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage")MultipartFile file,Model m,
			HttpSession session,Principal principal)
	{
		try {
			//old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			//image..
			if (!file.isEmpty())
			{
				//file work
				//rewrite
				//01-delete old photo  
				
				//02-update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			//message
			session.setAttribute("message", new Message("Your contact is updated..", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("CONTACT NAME : "+contact.getName());
		System.out.println("CONTACT ID : "+contact.getcId());
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//Your Profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title","Profile page");
		return "normal/profile";
	}
	
	
	//Your Profile handler
	@GetMapping("/settings")
	public String yourSettings(Model model) {
		
		model.addAttribute("title","Profile page");
		return "normal/settings";
	}
	
	

}
