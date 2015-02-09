(function () {
	$.getJSON("history.json", function(data){
		console.log(data);
		var grouped = _.groupBy(data, function(d){return d.index;});
		var sums = _.mapValues(grouped, function(values){
			return _.reduce(values, function(total, current){
				return total + current.load;
			}, 0);
		});
		var data = _.map(sums, function(value, key){
			return [parseInt(key), value];
		})
		console.log(grouped);
		console.log(sums);
		console.log(data);

		$.plot("#plot", [data]);
	});
})();
