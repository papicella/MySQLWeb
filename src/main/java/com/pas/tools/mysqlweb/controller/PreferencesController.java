package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
public class PreferencesController
{

    @GetMapping(value = "/prefs")
    public String showPrefs
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {

        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request to view preferences");

        UserPref userPref = (UserPref) session.getAttribute("prefs");

        model.addAttribute("userPref", userPref);

        return "preferences";
    }

    @PostMapping(value = "/prefs")
    public String handlePreferencesUpdates
            (@RequestParam(value="maxrecordsinsqlworksheet", required=true) String maxrecordsinsqlworksheet,
             @RequestParam(value="historysize", required=true) String historysize,
             @RequestParam(value="sampleDataSize", required=true) String sampleDataSize,
             Model model,
             HttpServletResponse response,
             HttpSession session) throws Exception
    {

        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request to Update Prefernces");

        UserPref userPref = (UserPref) session.getAttribute("prefs");
        userPref.setHistorySize(Integer.parseInt(historysize));
        userPref.setMaxRecordsinSQLQueryWindow(Integer.parseInt(maxrecordsinsqlworksheet));
        userPref.setSampleDataSize(Integer.parseInt(sampleDataSize));

        session.setAttribute("userPref", userPref);

        model.addAttribute("userPref", userPref);
        model.addAttribute("success", "Successfully updated preferences");

        return "preferences";

    }
}
