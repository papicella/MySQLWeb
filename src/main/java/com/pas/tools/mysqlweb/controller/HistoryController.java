package com.pas.tools.mysqlweb.controller;

import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.pas.tools.mysqlweb.beans.UserPref;
import com.pas.tools.mysqlweb.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HistoryController
{

    @GetMapping(value = "/history")
    public String showHistory
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request to show command history");
        UserPref userPref = (UserPref) session.getAttribute("prefs");

        String histAction = request.getParameter("histAction");

        if (histAction != null)
        {
            log.info("histAction = " + histAction);
            // clear history
            session.setAttribute("history", new LinkedList());

            model.addAttribute("historyremoved", "Successfully cleared history list");
        }

        LinkedList historyList = (LinkedList) session.getAttribute("history");

        int maxsize = userPref.getHistorySize();

        model.addAttribute("historyList", historyList.toArray());
        model.addAttribute("historysize", historyList.size());

        return "history";
    }
}
