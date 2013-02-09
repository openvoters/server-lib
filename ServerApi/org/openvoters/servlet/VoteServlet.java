/*  Open Voters - your opinion counts.
 *  Copyright (C) 2013 OpenVoters.org 
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openvoters.servlet;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openvoters.domain.Vote;

import com.google.gson.Gson;

/**
 * Servlet implementation class VoteServlet
 */
@WebServlet("/vote")
public class VoteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public VoteServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        BufferedReader buff = request.getReader();
        char[] buf = new char[4 * 1024]; // 4 KB char buffer
        int len;
        StringBuilder sb = new StringBuilder();

        while ((len = buff.read(buf, 0, buf.length)) != -1) {
            sb.append(buf, 0, len);
        }
        String vote = sb.toString();
        Gson gson = new Gson();

        Vote v = (Vote) gson.fromJson(vote, Vote.class);
        
                org.openvoters.VoteHandler vh = new org.openvoters.VoteHandler();
        try {
            vh.voteFor(v.getID(), v.getCandidate());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
