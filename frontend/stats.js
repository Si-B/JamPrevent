(function () {
	"use strict";
	window.setInterval(function(){
		$.getJSON("history.json", function(data){
			var grouped = _.groupBy(data, function(d){return d.index;});
			plotDifferences(grouped);
			plotSums(data);
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


	function sumLoads(data){
		return _.mapValues(data, function(values){
			return _.reduce(values, function(total, current){
				return total + current.load;
			}, 0);
		});

	}

	function plotSums(data){
		var sh = _.filter(data, function(d){return d.crossLocation === "SingleHeighest";});
		var ra = _.filter(data, function(d){return d.crossLocation === "Random";});
		var pr = _.filter(data, function(d){return d.crossLocation === "Predefined";});

		sh = _.groupBy(sh, function(d){return d.index;});
		ra = _.groupBy(ra, function(d){return d.index;});
		pr = _.groupBy(pr, function(d){return d.index;});


		var SingleHeighestSums = sumLoads(sh);
		var RandomSums = sumLoads(ra);
		var PredefinedSums = sumLoads(pr);

		var randomData = createArrayFromDict(RandomSums);
		var shData = createArrayFromDict(SingleHeighestSums);
		var prData = createArrayFromDict(PredefinedSums);

		$.plot("#plot", [
				{"label": "Zufällig", 
				 "data":  randomData
				},
				{"label": "Single Highest",
				 "data": shData
				},
				{"label": "Predefined",
				 "data": prData
				}
				]);
	}

})();
