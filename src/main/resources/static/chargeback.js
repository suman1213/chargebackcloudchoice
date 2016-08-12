/****************************HashTable Functionality**************************/

function HashTable(obj)
{
    this.length = 0;
    this.items = {};
    for (var p in obj) {
        if (obj.hasOwnProperty(p)) {
            this.items[p] = obj[p];
            this.length++;
        }
    }

    this.addItem = function(key, value)
    {
        var previous = undefined;
        if (this.hasItem(key)) {
            previous = this.items[key];
        }
        else {
            this.length++;
        }
        this.items[key] = value;
        return previous;
    }

    this.getItem = function(key) {
        return this.hasItem(key) ? this.items[key] : undefined;
    }

    this.hasItem = function(key)
    {
        return this.items.hasOwnProperty(key);
    }
   
    this.removeItem = function(key)
    {
        if (this.hasItem(key)) {
            previous = this.items[key];
            this.length--;
            delete this.items[key];
            return previous;
        }
        else {
            return undefined;
        }
    }

    this.keys = function()
    {
        var keys = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                keys.push(k);
            }
        }
        return keys;
    }

    this.values = function()
    {
        var values = [];
        for (var k in this.items) {
            if (this.hasItem(k)) {
                values.push(this.items[k]);
            }
        }
        return values;
    }

    this.each = function(fn) {
        for (var k in this.items) {
            if (this.hasItem(k)) {
                fn(k, this.items[k]);
            }
        }
    }

    this.clear = function()
    {
        this.items = {}
        this.length = 0;
    }
}
/************************************HashTable Ends******************************************/

var colors = [

"#00ffff", "#f0ffff", "#f5f5dc", "#000000", "#0000ff", "#a52a2a", "#00ffff",
		"#00008b", "#008b8b", "#a9a9a9", "#006400", "#bdb76b", "#8b008b",
		"#556b2f", "#ff8c00", "#9932cc", "#8b0000", "#e9967a", "#9400d3",
		"#ff00ff", "#ffd700", "#008000", "#4b0082", "#f0e68c", "#add8e6",
		"#e0ffff", "#90ee90", "#d3d3d3", "#ffb6c1", "#ffffe0", "#00ff00",
		"#ff00ff", "#800000", "#000080", "#808000", "#ffa500", "#ffc0cb",
		"#800080", "#800080", "#ff0000", "#c0c0c0", "#ffffff", "#ffff00" ];

var memoryConv=["B","KB","MB","GB","TB" ];
var memoryFig=[1024,1048576];
var h= new HashTable({one:1});

var selectedTab = "memoryTab";

var getcolorArray = function(labeList) {
	var ids = [];
	var colorArray = [];
	for (var i = 0; i < colors.length; ++i) {
		ids.push(colors[i]);
	}
	for (var i = 0; i < labeList.length; i++) {
		var random = Math.round(Math.random() * ids.length);
		var color = ids.splice(random, 1);
		colorArray.push(color[0])
	}
	console.log(colorArray);
	return colorArray;
}

var getOrganisations = function(){
	
	$.ajax({
		url : "getOrgList",
		success : function(data) {
			console.log(" Org List::" + data)
			populateOrgDropDown(data);
		}
	});
}

var populateOrgDropDown = function(vals){
	 $("#OrgSelect").empty();
	 $.each(vals, function(index, value){
		 $("#OrgSelect").append("<option>" + value + "</option>");
	 });
	 $("#OrgSelect").change(function(){
		var selectedOrg = $( "#OrgSelect option:selected" ).text();
		clearAllCharts();

		console.log(selectedOrg);
		 $.ajax({
				url : "getSpaceList/" + selectedOrg,
				success : function(data) {
					console.log(" Space List::" + data)
					populateSpaceDropDown(data);
				}
			});
		 
	 });
	 if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
			return;
		}else{
			 $.ajax({
					url : "getSpaceList/" + $( "#OrgSelect option:selected" ).text(),
					success : function(data) {
						console.log(" Space List::" + data)
						populateSpaceDropDown(data);
					}
				});
		}
}


var clearAllCharts = function(){
	
	var vals = ["cpu", "freecpu","memory", "unusedMemory", "disk"];
	$.each(vals, function(value, index) {
		if(h.getItem(value) != undefined){
			h.getItem(value).destroy();
		}
	})
}
	
var populateSpaceDropDown = function(vals){
	
	$("#OrgSpace").empty();
	var y = document.getElementsByClassName('active tablinks');
	console.log(y);
	 $.each(vals, function(index, value){
		 $("#OrgSpace").append("<option>" + value + "</option>");
	 });
	
	 $("#OrgSpace").change(function(){
			var selectedSpace = $( "#OrgSpace option:selected" ).text();
			clearAllCharts();
			console.log(selectedOrg);
			displayBasedOnTab(selectedTab);
			 
		 });
	 
	displayBasedOnTab(selectedTab);
}

var displayBasedOnTab = function(name){

	switch(name){
	case "memoryTab":
		getMemoryUsageDetails();
		getUnusedDetails();
		break;
	case "diskTab":
		getDiskUsageDetails();
		break;
	case "cpuTab":
		getCPUUsageDetails();
		getFreeCPUUsageDetails();
		break;
	}

}

var getMemoryUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$.ajax({
		url : "getResourceDetails/USED/MEM/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			populateChartDetails(data, "memory");
		}
	});

}

/*Getting Unused Memory Details*/
var getUnusedDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$.ajax({
		url : "getResourceDetails/UNUSED/MEM/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			populateChartDetails(data, "unusedMemory");
		}
	});

}
var getCPUUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$.ajax({
		url : "getResourceDetails/USED/CPU/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			populateChartDetails(data, "cpu");
		}
	});

}

var getFreeCPUUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$.ajax({
		url : "getResourceDetails/UNUSED/CPU/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			populateChartDetails(data, "freeCPU");
		}
	});

}
/* Getting CPU Usage*/

/* Getting Disk Usage*/
var getDiskUsageDetails = function() {
	if($( "#OrgSelect option:selected" ).text() ==="" || $( "#OrgSelect option:selected" ).text() === undefined || $( "#OrgSelect option:selected" ).text() === null){
		return;
	}
	if($( "#OrgSpace option:selected" ).text() ==="" || $( "#OrgSpace option:selected" ).text() === undefined || $( "#OrgSpace option:selected" ).text() === null){
		return;
	}
	$.ajax({
		url : "getResourceDetails/USED/DISK/" + $( "#OrgSelect option:selected" ).text() + "/" +$( "#OrgSpace option:selected" ).text(),
		success : function(data) {
			populateChartDetails(data, "disk");
		}
	});

}


/* Utility function to create a String Array*/

var getlabelsArray = function(labeList) {
	var labelsArray = [];
	for (var i = 0; i < labeList.length; i++) {
		labelsArray.push(labeList[i]);
	}
	return labelsArray;
};

/* Utility Function to create an Integer Array*/

var getdataArray = function(data) {
	var dataArray = [];
	for (var i = 0; i < data.length; i++) {
		dataArray.push(parseFloat(data[i]));
	}
	return dataArray;
};

/* Function to populate chart Details */
var populateChartDetails = function(data, id) {
	var canvasId = document.getElementById(id);
	var colorArray = getcolorArray(data.label);
	var chartData = {
		labels : getlabelsArray(data.label),
		datasets : [ {
			data : getdataArray(data.data),
			backgroundColor : colorArray,
			hoverBackgroundColor : colorArray
		} ]
	};
	
	var ctx = canvasId.getContext("2d");
	var midX = canvasId.width / 2;
	var midY = canvasId.height / 2;
	var totalValue = getTotalValue(data.data);
console.log(chartData);
    
if(h.getItem(id) != undefined){
	h.getItem(id).destroy();
}


    var pieChart = new Chart(canvasId, {
		type : 'pie',
		data : chartData,
		options : {
			responsive : false,
			//onAnimationProgress:  drawSegmentValues,
				tooltips : {
				callbacks : {
					label : function(tooltipItem, data) {
						var dataset = data.datasets[tooltipItem.datasetIndex];
						var total = dataset.data.reduce(function(previousValue,
								currentValue, currentIndex, array) {
							return previousValue + currentValue;
						});
						var currentValue = dataset.data[tooltipItem.index];
						var returnElement;
						if(!id.toUpperCase().includes("CPU")){
							if(currentValue>=  memoryFig[1]){
								return ((currentValue/parseInt(memoryFig[1])).toFixed(2) + memoryConv[2]);
							}else if(currentValue>=memoryFig[0] && currentValue<  memoryFig[1] ){
								return (currentValue + memoryConv[1]);
							}else if(currentValue<memoryFig[0]){
								return (currentValue + memoryConv[0]);
							} 
						}else{
							var precentage = Math.floor(((currentValue / total) * 100) + 0.5);
							return precentage + "%";
						}
						
					}
				}
			}

		}
	});
 h.addItem(id, pieChart);
	var radius = pieChart.outerRadius;

	function drawSegmentValues() {

		for (var i = 0; i < pieChart.segments.length; i++) {
			ctx.fillStyle = "white";
			var textSize = canvasId.width / 15;
			ctx.font = textSize + "px Verdana";
			// Get needed variables
			var value = pieChart.segments[i].value / totalValue * 100;
			if (Math.round(value) !== value)
				value = (pieChart.segments[i].value / totalValue * 100)
						.toFixed(1);
			value = value + '%';

			var startAngle = pieChart.segments[i].startAngle;
			var endAngle = pieChart.segments[i].endAngle;
			var middleAngle = startAngle + ((endAngle - startAngle) / 2);

			// Compute text location
			var posX = (radius / 2) * Math.cos(middleAngle) + midX;
			var posY = (radius / 2) * Math.sin(middleAngle) + midY;

			// Text offside by middle
			var w_offset = ctx.measureText(value).width / 2;
			var h_offset = textSize / 4;

			ctx.fillText(value, posX - w_offset, posY + h_offset);
		}

	}
	/*End of draw Segment*/
}

function getTotalValue(arr) {
	var total = 0;
	for (var i = 0; i < arr.length; i++)
		total += arr[i];
	return total;
}



function getDropDownList( id, optionList) {
	
		var selectElement= document.getElementById(id);
		for (var i = 0; i<=optionList.length-1; i++){
		    var opt = document.createElement('option');
		    opt.value = optionList[i];
		    opt.innerHTML = optionList[i];
		    selectElement.appendChild(opt);
		}
}
function openTab(evt, tabName) {
    var i, tabcontent, tablinks;
  //  var tab = document.getElementsByName(tabName);
    selectedTab = tabName;
    document.getElementById(tabName).style.visibility  = 'visible';
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }
    document.getElementById(tabName).style.display = "block";
    evt.currentTarget.className += " active";
    
}