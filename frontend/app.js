/*eslint-env browser*/
/*eslint no-use-before-define:0 */
/*global $*/

(function(){
  "use strict";
  $(document).ready(function(){
    setInterval(updatePage, 100);
  });


  function updatePage(){
    $.getJSON("./state.json", updateImage);
  }

  function updateLights(data, selector){
	  var random = document.getElementById(selector);
    $.each(data, function(index, value){
      random.getElementsByClassName(value.location + "2" + value.direction)[0].style.fill = value.state;
      random.getElementsByClassName(value.location + "2" + value.direction + "-load")[0].innerHTML = value.load;
    });
  }

  function updateImage(data){
	var sh = _.filter(data, function(d){return d.crossLocation === "SingleHeighest";});
	var ra = _.filter(data, function(d){return d.crossLocation === "Random";});
	var pr = _.filter(data, function(d){return d.crossLocation === "Predefined";});
	var rp = _.filter(data, function(d){return d.crossLocation === "RandomPredefined";});
    updateLights(sh, "singleHighest");
    colorHighest(sh, "singleHighest");
    createStatistics(sh, "singleHighest");

    updateLights(ra, "random");
    colorHighest(ra, "random");
    createStatistics(ra, "random");

    updateLights(pr, "predefined");
    colorHighest(pr, "predefined");
    createStatistics(pr, "predefined");

    updateLights(pr, "randomPredefined");
    colorHighest(pr, "randomPredefined");
    createStatistics(pr, "randomPredefined");
  }

  function createStatistics(data, selector){
	  var random = document.getElementById(selector);
    var totalCars = Array.reduce(data, function(total, current){
    return current.load + total;}, 0);
    random.getElementsByClassName("totalCars")[0].innerHTML = totalCars;
  }


  function colorHighest(data, selector){
	  var random = document.getElementById(selector);
    var carCounts = $.map(data, function(d){return d.load});
    var highestCarCount = Array.max(carCounts);
    $.each(data, function(index, value){
      var color = value.load === highestCarCount ? "orange": "black";
      random.getElementsByClassName(value.location + "2" + value.direction + "-load")[0].style.fill = color;
    });
  }


  Array.max = function( array ){
    return Math.max.apply( Math, array );
  };
})();

