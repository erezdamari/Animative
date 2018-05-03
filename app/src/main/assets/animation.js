var points = [{x : 60, y : 40},
			  {x : 80, y : 30},
			  {x : 100, y : 35},
			  {x : 120, y : 50},
			  {x : 130, y : 80},
			  {x : 150, y : 95},
			  {x : 170, y : 93},
			  {x : 190, y : 85},
			  {x : 200, y : 82},
			  {x : 220, y : 83},
			  {x : 240, y : 95},
			  {x : 250, y : 110},
			  {x : 260, y : 130},
			  {x : 270, y : 150},
			  {x : 280, y : 170},
			  {x : 290, y : 190}];

function animate(){
	var canvas = document.getElementById("myCanvas");
    var ctx = canvas.getContext("2d");
	
	var img = document.createElement("img");
	img.src = "animation.bmp"
	img.style = "style = margin: 20% 0% 0% 20%;";
	//canvas.style="border:3px solid #d3d3d3;"
	//canvas.style.width = window.innerWidth + ';';
	//canvas.style.height = window.innerHeight + ';';
	//canvas.setAttribute("style", "width:" + window.innerWidth + ";height:" + window.innerHeight + ";border:1px solid #d3d3d3;");
	//canvas.style = "width:250px; height:300px; border:1px solid #d3d3d3;"

	moveTo(ctx, img, 1);
}

function moveTo(ctx, img, i){
	ctx.drawImage(img, points[i].x, points[i].y);
	ctx.stroke();
	i++;
	if(i<16)
	{
		setTimeout(moveTo, 30, ctx, img, i);
	}
}