package com.medman.controllers;

import com.medman.models.*;
import com.medman.models.PrescriptionRepository;
import org.apache.tomcat.util.http.parser.MediaType;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.jws.soap.SOAPBinding;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    PrescriptionRepository prescriptionsDao;

    @Autowired
    MedicationRepository medicationsDao;

    @Autowired
    Appointments appointmentsDao;

    @Autowired
    Messages messageDao;

    @Autowired
    DoctorPatients docPatientDao;


    @GetMapping("/dashboard")
    public String showDash(Model model) {

        model.addAttribute("user", loggedInUser());
        model.addAttribute("prescription", new Prescription());
        model.addAttribute("appointment", new AppointmentTime());
        model.addAttribute("prescriptions", prescriptionsDao.findByPatient(loggedInUser().getId()));
        model.addAttribute("medications", medicationsDao.findAll());
        model.addAttribute("lowSupplyPrescriptions", prescriptionsDao.findByDaySupplyAlert(loggedInUser().getId()));
        // add to model list of prescriptions with low daysSupply to display in alert panel

        return "shared/dashboard";
    }

    @PostMapping("/addPrescription")
    public String addMedication(
            @Valid Prescription prescription,
            @RequestParam(name = "medicationId") Long medicationId,
            Errors validation,
            Model model
    ) {
        if (validation.hasErrors()) {
            model.addAttribute("errors", validation);
            model.addAttribute("prescriptions", prescription);
            return "shared/dashboard";
        }
        prescription.setUser(loggedInUser());
        prescription.setMedication(medicationsDao.findOne(medicationId));
        prescriptionsDao.save(prescription);
        model.addAttribute("prescriptions", new Prescription());
        return "redirect:/dashboard";
    }

    @PostMapping("/dashboard/medTaken")
    public String takenMed(@RequestParam("id") Long id) {
        Prescription currentPr = prescriptionsDao.findOne(id);
        if (currentPr.getDosageFrequency() == 0) {
            currentPr.setDosageFrequency(currentPr.getPrescribedQuantity()/currentPr.getDaySupply());
        }
        currentPr.setPillsTaken(currentPr.getPillsTaken() + 1);
        if (currentPr.getPillsTaken().equals(currentPr.getDosageFrequency())) {
            currentPr.setPillsTaken((long) 0);
            currentPr.setDaySupply(currentPr.getDaySupply() - 1);
        }
        prescriptionsDao.save(currentPr);
        // when daySuuply hits a certain number, we should send a text message here.
        // prescriptions with low daysSupply should be calculated here and placed in a list.
        return "redirect:/dashboard";
    }

    @GetMapping("/my_doctors")
    public String showMyDoctors(Model model) {
//        model.addAttribute("doctors", docPatientDao.findDoctorsByUser(loggedInUser().getId()));

        return "shared/viewLinkedUsers";
    }

    @PostMapping("/my_doctors")
    public String setDoctor(@RequestParam("docKey") Long docKey) {
        DoctorPatientRelationship dpr = new DoctorPatientRelationship();
        System.out.println("dockey "+ docKey );
        dpr.setPatient(loggedInUser().getId());
        System.out.println("user id " + loggedInUser().getId());
        System.out.println("doctors id " + usersDao.findByDocNum(docKey).getId());
        dpr.setDoctor(usersDao.findByDocNum(docKey).getId()); // this needs to use the "docKey"
        docPatientDao.save(dpr);
        return "shared/viewLinkedUsers";
    }

    @GetMapping("/messages")
    public String showMessages(Model model) {
        model.addAttribute("message", new Message());
        return "shared/messages";
    }

    @PostMapping("/messages")
    public String sendMessage(
            @Valid Message message,
            Errors validation,
            Model model
    ) {
        if (validation.hasErrors()) {
            System.out.println(message.getMessageContent());
            model.addAttribute("errors", validation);
            model.addAttribute("message", message);
            return "shared/dashboard";
        }
//        message.setUser(loggedInUser()); work this in somehow
        messageDao.save(message);
        return "redirect:/dashboard";

    }

    @GetMapping("/edit")
    public String editPage(Model model) {
        User user = usersDao.findOne(loggedInUser().getId());
        if (user.getId() != loggedInUser().getId()) {
            return "/login";
        }
        model.addAttribute("user", user);
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
            return "posts/edit";
        }

        User newUser = usersDao.findByUsername(loggedInUser().getUsername());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
//        newUser.setDateOfBirth(user.getDateOfBirth());
//        newUser.setPhoneNumber(user.getPhoneNumber());
//        newUser.setStreetAddress(user.getStreetAddress());
//        newUser.setCity(user.getCity());
//        newUser.setState(user.getState());
//        newUser.setZipCode(user.getZipCode());
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setEmail(user.getEmail());


        usersDao.save(newUser);
        return "redirect:/dashboard";

    }

    @PostMapping("/register")
    public String registerUser(
            @Valid User user,
            Errors validation,
            Model model
    ) {
        if (validation.hasErrors()) {
            model.addAttribute("errors", validation);
            model.addAttribute("user", user);
            return "register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        usersDao.save(user);
        return "redirect:/about";

    }

    @GetMapping("/login?logout")
    public String logout() {
        return "redirect:/";
    }

    @PostMapping("/addAppointment")
    public String addAppointment(@Valid AppointmentTime appointmentTime, Errors validation, Model model) {
        if (validation.hasErrors()) {
            model.addAttribute("errors", validation);
            model.addAttribute("appointment", appointmentTime);
            return "shared/dashboard";
        }

        appointmentTime.setUser(loggedInUser());
        appointmentsDao.save(appointmentTime);
        model.addAttribute("appointment", new AppointmentTime());
        return "redirect:/dashboard";
    }
}



