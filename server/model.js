
const {Datastore} = require('@google-cloud/datastore');

const datastore = new Datastore();



function insertAttendance(attendance) {
	return datastore.save({
		key: datastore.key('attendance'),
		data: attendance,
	});
};


function getAttendances() {
	const query = datastore
		.createQuery('attendance')
		.order('timestamp', {descending: true})
		.limit(10);

	return datastore.runQuery(query);
};

function getAttendances_between(startDate, endDate) {
	const query = datastore
	.createQuery('attendance')
	.filter('timestamp', '>=', startDate)
	.filter('timestamp', '<', endDate)
	
	return datastore.runQuery(query)
}

function countEntities([entities]) {
	return entities.length > 0
}




module.exports = {
	insertAttendance,
	getAttendances,
	getAttendances_between,
	countEntities
}

