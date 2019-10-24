/*jslint node: true */
"use strict";

const http = require("http");
const url = require("url");
const moment = require("moment-timezone");
const model = require("./model");

console.log("==============================");
console.log("Server starting.......");


function getStartOf_Local_Today() {
    return moment().tz("America/New_York").startOf("day").toDate();
}


function getEndOf_Local_Today() {
    const startOfToday = getStartOf_Local_Today();

    return moment(startOfToday).add(1, "day").toDate();	
}


// check if todays_attendance already has a date with
// the same calendar day as currentDate
async function checkIf_attendanceTakenToday(email) { 
    const startOfToday = getStartOf_Local_Today();
    const endOfToday = getEndOf_Local_Today();
	
    const [entities] = await model.getAttendances_between(
        startOfToday,
        endOfToday,
        email
    );		

    return entities.length > 0;
}


async function handle_take_attendance(request, response) {   
	try{		
		const parsed_url = url.parse(request.url);
        const message = parsed_url.pathname;
		const email = message.split("/")[2];     
        
        const flag = await checkIf_attendanceTakenToday(email);
	  
		if (flag === true) {
			console.log("Attendance has already been taken today");           
		} else {
            console.log("Attendence not taken today... Taking attendance");	
            console.log(`requested url frm browser :${request.url}`);
                
            const attendance = {
                email: email,
                timestamp: new Date()						
			};                
            
            model.insertAttendance(attendance);
		}
		response.end();   
        
	}catch(err)	{
		console.log(err);
		console.log(err.message);
		
		response.end();      
	}	
}


async function handle_read_attendance(request, response) {
	try{       
        const [attendances] = await model.getAttendances();          
        const timestamps = attendances.map(
            (entity) => moment(entity.timestamp).tz("America/New_York").toString()
            );                
        const emails = 	attendances.map(
            (entity_email) => entity_email.email
            );              
            
        const timestamps_html = timestamps.join("<br />\n");
        const email_html = emails.join("<br />\n");        
        const responseBody = `<table><tr><td>${timestamps_html}</td>&nbsp;<td>${email_html}</td></tr></table>`;
        
        response.setHeader("Content-Length", responseBody.length);
        response.write(responseBody);
        response.end();    
        
	}catch(err){
		console.log(err);
		console.log(err.message);
		
		response.end();		
	}	
}


const process_request = function (request, response) {
    console.log(`requested url frm browser :${request.url}`);	
    const parsed_url = url.parse(request.url);
	
    if (parsed_url.pathname.startsWith("/take_attendance")) {
        handle_take_attendance(request, response);
		console.log(parsed_url.pathname)
    }else if (parsed_url.pathname === "/read_attendance") {
        handle_read_attendance(request, response);
    }
};


//create a server object:
const server = http.createServer(process_request);
const PORT = process.env.PORT || 8080;
server.listen(PORT); //the server object listens on port 8080
