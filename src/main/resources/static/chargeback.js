
var colors = [

"#00ffff", "#f0ffff", "#f5f5dc", "#000000", "#0000ff", "#a52a2a", "#00ffff",
		"#00008b", "#008b8b", "#a9a9a9", "#006400", "#bdb76b", "#8b008b",
		"#556b2f", "#ff8c00", "#9932cc", "#8b0000", "#e9967a", "#9400d3",
		"#ff00ff", "#ffd700", "#008000", "#4b0082", "#f0e68c", "#add8e6",
		"#e0ffff", "#90ee90", "#d3d3d3", "#ffb6c1", "#ffffe0", "#00ff00",
		"#ff00ff", "#800000", "#000080", "#808000", "#ffa500", "#ffc0cb",
		"#800080", "#800080", "#ff0000", "#c0c0c0", "#ffffff", "#ffff00" ];

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
var getMemoryUsageDetails = function() {

	$.ajax({
		url : "getDetails",
		success : function(data) {
			populateChartDetails(data, "memory");
		}
	});

}

/*Getting Unused Memory Details*/
var getUnusedDetails = function() {

	$.ajax({
		url : "getUnusedDetails",
		success : function(data) {
			populateChartDetails(data, "unusedMemory");
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
		dataArray.push(parseInt(data[i]));
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
						var precentage = Math
								.floor(((currentValue / total) * 100) + 0.5);
						return precentage + "%";
					}
				}
			}

		}
	});

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
