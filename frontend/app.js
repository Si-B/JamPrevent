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

  function updateLights(data){
    $.each(data, function(index, value){
      document.getElementById(value.location + "2" + value.direction).style.fill = value.state;
      document.getElementById(value.location + "2" + value.direction + "-load").innerHTML = value.load;
    });
  }

  function updateImage(data){
    updateLights(data);
    colorHighest(data);
  }


  function colorHighest(data){
    var carCounts = $.map(data, function(d){return d.load});
    var highestCarCount = Array.max(carCounts);
    $.each(data, function(index, value){
      var color = value.load === highestCarCount ? "yellow": "black";
      document.getElementById(value.location + "2" + value.direction + "-load").style.fill = color;
    });
  }


  Array.max = function( array ){
    return Math.max.apply( Math, array );
  };
})();

