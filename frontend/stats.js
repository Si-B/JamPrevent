(function () {
	"use strict";
	window.setInterval(function(){
		$.getJSON("history.json", function(data){
			var grouped = _.groupBy(data, function(d){return d.index;});
			plotDifferences(grouped);
			plotSums(grouped);
		});
	}, 100);

	function createArrayFromDict(data){
		return _.map(data, function(value, key){
			return [parseInt(key), value];
		});

	}

	function plotDifferences(grouped){
		var differences = _.mapValues(grouped, function(values){
			var minLoad = _.min(values, function(d){return d.load;}).load;
			var maxLoad = _.max(values, function(d){return d.load;}).load;
			return  maxLoad - minLoad;
		});

		differences = createArrayFromDict(differences);

		$.plot("#differences", [differences]);
	}

	function plotSums(grouped){
		var sums = _.mapValues(grouped, function(values){
			return _.reduce(values, function(total, current){
				return total + current.load;
			}, 0);
		});

		var data = createArrayFromDict(sums);

		$.plot("#plot", [data]);
	}

})();
