package com.pas.tools.mysqlweb.controller;

import com.pas.tools.mysqlweb.utils.Themes;
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
public class ThemeController
{
    @GetMapping(value = "/selecttheme")
    public String alterTheme
            (Model model, HttpServletResponse response, HttpServletRequest request, HttpSession session) throws Exception
    {
        if (Utils.verifyConnection(response, session))
        {
            log.info("user_key is null OR Connection stale so new Login required");
            return null;
        }

        log.info("Received request alter theme");

        String selectedTheme = request.getParameter("theme");

        if (selectedTheme != null)
        {
            if (selectedTheme.trim().length() != 0)
            {
                switch (selectedTheme) {
                    case "default":
                        session.setAttribute("themeMain", Themes.defaultTheme);
                        session.setAttribute("themeMin", Themes.defaultThemeMin);
                        break;
                    case "cyborg":
                        session.setAttribute("themeMain", Themes.defaultThemeCyborg);
                        session.setAttribute("themeMin", Themes.defaultThemeCyborgMin);
                        break;
                    case "sandstone":
                        session.setAttribute("themeMain", Themes.defaultThemeSandstone);
                        session.setAttribute("themeMin", Themes.defaultThemeSandstoneMin);
                        break;
                    case "slate":
                        session.setAttribute("themeMain", Themes.defaultThemeSlate);
                        session.setAttribute("themeMin", Themes.defaultThemeSlateMin);
                        break;
                    case "spacelab":
                        session.setAttribute("themeMain", Themes.defaultThemeSpacelab);
                        session.setAttribute("themeMin", Themes.defaultThemeSpacelabMin);
                        break;
                }

                session.setAttribute("theme", selectedTheme);
                log.info("New theme set as : " + selectedTheme);
            }

        }

        return "main";
    }
}
