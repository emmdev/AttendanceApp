/*jslint node: true */
"use strict";

const {Datastore} = require("@google-cloud/datastore");
const datastore = new Datastore();


function insertAttendance(attendance) {
    return datastore.save({
        key: datastore.key("attendance"),
        data: attendance,
    });
}


function getAttendances() {
    const query = datastore
        .createQuery("attendance")
        .order("timestamp", {descending: true})
        .limit(100);

    return datastore.runQuery(query);
}


function getAttendances_between(startDate, endDate, email) {
    const query = datastore
        .createQuery("attendance")
        .filter("timestamp", ">=", startDate)
        .filter("timestamp", "<", endDate)
        .filter ("email", "=", email);

    return datastore.runQuery(query);
}


module.exports = {
    insertAttendance,
    getAttendances,
    getAttendances_between,
};
