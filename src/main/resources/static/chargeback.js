
var colors = [
     "#00ffff",
     "#f0ffff",
     "#f5f5dc",
     "#000000",
     "#0000ff",
     "#a52a2a",
    "#00ffff",
     "#00008b",
     "#008b8b",
     "#a9a9a9",
     "#006400",
     "#bdb76b",
     "#8b008b",
     "#556b2f",
     "#ff8c00",
     "#9932cc",
     "#8b0000",
     "#e9967a",
     "#9400d3",
     "#ff00ff",
     "#ffd700",
     "#008000",
     "#4b0082",
     "#f0e68c",
     "#add8e6",
    "#e0ffff",
     "#90ee90",
     "#d3d3d3",
     "#ffb6c1",
    "#ffffe0",
    "#00ff00",
    "#ff00ff",
    "#800000",
    "#000080",
    "#808000",
    "#ffa500",
    "#ffc0cb",
     "#800080",
     "#800080",
     "#ff0000",
     "#c0c0c0",
     "#ffffff",
     "#ffff00"
];


var getcolorArray =  function(labeList){
	var ids = [];
	var colorArray =  [];
for (var i = 0; i < colors.length; ++i){
    ids.push(colors[i]);
}
for (var i=0; i < labeList.length; i++) {
var random = Math.round(Math.random() * ids.length);
var color = ids.splice(random, 1);
colorArray.push(color)
}
return colorArray;
}
var getMemoryUsageDetails = function(){
	
	 $.ajax({
		    url:"getDetails",  
		    success:function(data) {
		      populateChartDetails(data); 
		    }
		  });
 
}

/* Utility function to create a String Array*/
var labelsArray = [];
var getlabelsArray = function(labeList){
	
	for (var i=0; i < labeList.length; i++) {
		labelsArray.push(labeList[i]);
	}
	return labelsArray;
};


/* Utility Function to create an Integer Array*/
var dataArray = [];
var getdataArray = function(data){
	
	for (var i=0; i < data.length; i++) {
		dataArray.push(parseInt(data[i]));
	}
	return dataArray;
};


/* Function to populate chart Details */
var populateChartDetails = function(data){
	 var ctx = document.getElementById("memory");
	 var colorArray = getcolorArray(data.label);
	 alert(data);
	 var chartData =  {
     	    labels: getlabelsArray(data.label),
         	    datasets: [
         	        {
         	        	data: getdataArray(data.data),
         	            backgroundColor:colorArray,
         	            hoverBackgroundColor: colorArray
         	        }]
         	};
	 var myPieChart = new Chart(ctx,{
         type: 'pie',
         data: chartData,
         options: {
		        responsive: false
		    }
     });
	 
	
	 
}