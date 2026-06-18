package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class EndpointsController
{

    @GetMapping(value = "/endpoints")
    public String endpointsPage
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Invoking Endpoints Controller...");

        model.addAttribute("propertyMap", Utils.jvmPropertyMap());

        model.addAttribute("vcapServices", Utils.getEnvMap("VCAP_SERVICES"));
        model.addAttribute("vcapApplication", Utils.getEnvMap("VCAP_APPLICATION"));

        return "endpoints";
    }
}