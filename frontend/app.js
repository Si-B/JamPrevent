$(document).ready(function(){
  setInterval(updatePage, 100);
});


function updatePage(){
  $.getJSON('./state.json', updateImage);
}
//function (data, textStatus, jqXHR) {



function updateImage(data){
  $.each(data, function(index, value){
    document.getElementById(value.location).style.fill = value.state;
  });
}
