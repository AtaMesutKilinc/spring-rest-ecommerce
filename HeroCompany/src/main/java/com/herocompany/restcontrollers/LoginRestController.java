package com.herocompany.restcontrollers;

import com.herocompany.entities.Admin;
import com.herocompany.entities.Customer;
import com.herocompany.entities.Login;
import com.herocompany.services.CustomerService;
import com.herocompany.services.UserDetailService;
import com.herocompany.utils.Utility;
import net.bytebuddy.utility.RandomString;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;

@RestController
public class LoginRestController {
    final UserDetailService userDetailService;
    private JavaMailSender mailSender;
    private CustomerService customerService;


    public LoginRestController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @PostMapping("/login")
    public ResponseEntity login (@Valid @RequestBody Login login){
        return  userDetailService.login(login);
    }


    @PostMapping("/register") //save işlemi
    public ResponseEntity register(@Valid @RequestBody Customer customer){
        return userDetailService.registerCustomer(customer);
    }
    @PostMapping("/registerAdmin")
    public ResponseEntity register(@Valid @RequestBody Admin admin){
        return userDetailService.registerAdmin(admin);
    }


    //şifremi unuttum kızmı
    @GetMapping("/forgot_password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("pageTitle","ForgotPassword");
        return "forgot_password_form";

    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(HttpServletRequest request, Model model) {
        String email=request.getParameter("email");
        String token= RandomString.make(45);
        System.out.println("Email :"+email);
        System.out.println("Token :"+token);
        try {
            customerService.updateResetPasswordToken(token,email);
            String resetPasswordLink= Utility.getSiteURL(request)+"/reset_password?token";
            System.out.println(resetPasswordLink);
            sendEmail(email,resetPasswordLink);
        } catch (Exception e) {
            model.addAttribute("error",e.getMessage());
        }
        return "forgot_password_form";

    }

    //email yollyo
    public void sendEmail(String email,String resetPasswordLink) throws UnsupportedEncodingException, MessagingException {
        MimeMessage message=mailSender.createMimeMessage();
        MimeMessageHelper helper=new MimeMessageHelper(message);
        helper.setFrom("gltknulas96@gmail.com","Support");
        helper.setTo(email);
        String subject="here's thelink to reset your password";
        String content=resetPasswordLink;
        helper.setSubject(subject);
        helper.setText(content,true);
        mailSender.send(message);
    }


    @GetMapping("/reset_password")
    public String showResetPasswordForm(@Param(value = "token") String token, Model model) {

        Customer customer=customerService.getByResetPasswordToken(token);
        if (customer==null){
            model.addAttribute("title","Reset your password");
            model.addAttribute("message","Invalid Token");
            return "message";

        }
        model.addAttribute("token",token);
        return "reset_password_form";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(HttpServletRequest request, Model model) {
        String token=request.getParameter("token");
        String password=request.getParameter("password");
        Customer customer=customerService.getByResetPasswordToken(token);
        if (customer==null){
            model.addAttribute("title","Reset your password");
            model.addAttribute("message","Invalid Token");
            return "message";

        }
        model.addAttribute("token",token);
        return "message";



    }

}
