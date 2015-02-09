(function () {
	window.setInterval(function(){
		$.getJSON("history.json", function(data){
			var grouped = _.groupBy(data, function(d){return d.index;});

			var differences = _.mapValues(grouped, function(values){
				var minLoad = _.min(values, function(d){return d.load;}).load;
				var maxLoad = _.max(values, function(d){return d.load;}).load;
				return  maxLoad - minLoad;
			});

			differences = _.map(differences, function(value, key){
				return [parseInt(key), value];
			});

			$.plot("#differences", [differences]);

			console.log(differences);

			var sums = _.mapValues(grouped, function(values){
				return _.reduce(values, function(total, current){
					return total + current.load;
				}, 0);
			});
			var data = _.map(sums, function(value, key){
				return [parseInt(key), value];
			})

			$.plot("#plot", [data]);
		});
	}, 100);
})();
