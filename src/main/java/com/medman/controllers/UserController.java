package com.medman.controllers;

import com.medman.models.*;
import com.medman.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by jessedavila on 1/17/17.
 */
@Controller
//@RequestMapping("/user")
public class UserController extends BaseController {


    @Autowired
    Users usersDao;

    @Autowired
    Roles roles;

    @Autowired
    Appointments appointmentsDao;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String showDash(Model model) {
        // need to add objects for alerts, meds, and dates, so 3 model.addAttribute?
        return "shared/dashboard";
    }

    @GetMapping("/my_doctors")
    public String showMyDoctors(Model model) {
        // model.addAttribute("users", new User()); Need to call on user relationships between patient and doctor.
        // current logged in user's connected users should be called here and it should show their info.
        return "shared/viewLinkedUsers";
    }

    @GetMapping("/messages")
    public String showMessages(Model model) {
        //model.addAttribute("") are we making objects for all of these different tables? we must be? so a message instance is passed here?
        //model.addAttribute() and also a user object, this will be fairly complicated to show many message streams and select one to show more messages
        return "shared/messages";
    }

    @GetMapping("/edit")
    public String editPage(Model model) {
        User user = usersDao.findOne(loggedInUser().getId());
        if(user.getId() != loggedInUser().getId()){
            return "/";
        }
        model.addAttribute("user", user); // need to call logged in User
        return "shared/profile"; // only a logged in user can go to user/edit

    }

    @PostMapping("/edit")
    public String editUserInfo(
            @Valid User user,
            Errors validation,
            Model model
    ) {
        if (validation.hasErrors()) {
            model.addAttribute("errors", validation);
            model.addAttribute("user", user);
            return "shared/profile";
        }

        User newUser = usersDao.findByUsername(loggedInUser().getUsername());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setDateOfBirth(user.getDateOfBirth());
        newUser.setPhoneNumber(user.getPhoneNumber());
        newUser.setStreetAddress(user.getStreetAddress());
        newUser.setCity(user.getCity());
        newUser.setState(user.getState());
        newUser.setZipCode(user.getZipCode());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setEmail(user.getEmail());


        usersDao.save(newUser);
        return "shared/dashboard";

    }

    @PostMapping("/register")
    public String registerUser(@Valid User user, Errors validation, Model model){
        if(validation.hasErrors()){
            model.addAttribute("errors", validation);
            model.addAttribute("user", user);
            return "register";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersDao.save(user);
        return "redirect:/about";

    }

    @GetMapping("/login?logout")
    public String logout(){return "redirect:/";}

    @PostMapping("/addAppointment")
    public String addAppointment(@Valid AppointmentTime appointmentTime, Errors validation, Model model){
        if(validation.hasErrors()){
            model.addAttribute("errors", validation);
            model.addAttribute("appointments", appointmentTime);
            return "shared/dashboard";
        }

        appointmentTime.setUser(loggedInUser());
        appointmentsDao.save(appointmentTime);
        return "redirect:shared/dashboard";


    }



}
