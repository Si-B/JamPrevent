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
  //function (data, textStatus, jqXHR) {



  function updateImage(data){
    $.each(data, function(index, value){
      document.getElementById(value.location + "2" + value.direction).style.fill = value.state;
      document.getElementById(value.location + "2" + value.direction + "-load").innerHTML = value.load;
    });
  }
})();
