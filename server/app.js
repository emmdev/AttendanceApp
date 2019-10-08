
'use strict';

const http = require('http');
const url = require('url');


const model = require('./model')


console.log("==============================")
console.log("Server starting.......")



function convertDate_to_EasternTimezone(date) {
	date = date.toLocaleString("en-US", {timeZone: "America/New_York"});
	
	return new Date(date);
}


function getStartOf_Local_Today() {
	const CURRENT_TIMEZONE_OFFSET = 4 // CHANGE this in November
	
	const now = new Date()
	const hoursOffset = CURRENT_TIMEZONE_OFFSET //now.getTimezoneOffset() / 60
	const startOfToday_Epoch = Date.UTC(now.getFullYear(), now.getMonth(), now.getDate(), hoursOffset, 0, 0)
	
	return new Date(startOfToday_Epoch)
}

function getEndOf_Local_Today() {
	const startOfToday = getStartOf_Local_Today()

	return new Date(startOfToday.getTime() + 24*60*60*1000)
}


// check if todays_attendance already has a date with the same calendar day as currentDate
async function checkIf_attendanceTakenToday() {

	const startOfToday = getStartOf_Local_Today()

	const endOfToday = getEndOf_Local_Today()

	const entities = await model.getAttendances_between(startOfToday, endOfToday)
	
	return model.countEntities(entities)
}




async function handle_take_attendance(request, response) {
	const flag = await checkIf_attendanceTakenToday()
	
	if (flag == true)
	{
		console.log("Attendance has already been taken today")
	}
	else
	{
		console.log("Attendence not taken today... Taking attendance")
		
		const attendance = {
			timestamp: new Date(),
		};
		model.insertAttendance(attendance)
	}

	response.end(); 
}

async function handle_read_attendance(request, response) {
	const [attendances] = await model.getAttendances()
	
	const timestamps = attendances.map(
		(entity) => convertDate_to_EasternTimezone(entity.timestamp).toLocaleString()
	)
	
	const timestamps_html = timestamps.join('<br />\n')
	const responseBody = '<p>'+timestamps_html+'</p>'

	response.setHeader('Content-Length', responseBody.length)
	response.write(responseBody); 
	response.end();
}




const process_request = function (request, response) {
	console.log("requested url frm browser :" + request.url);
	
	const parsed_url = url.parse(request.url)
	
	if(parsed_url.pathname === '/take_attendance')
	{
		handle_take_attendance(request, response);
	}
	else if(parsed_url.pathname === '/read_attendance')
	{
		handle_read_attendance(request, response)
	}
}

//create a server object:
const server = http.createServer(process_request);
const PORT = process.env.PORT || 8080;
server.listen(PORT); //the server object listens on port 8080

