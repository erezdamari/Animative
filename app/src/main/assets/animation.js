var points;

function CreatePointsArray(pointsList){
    points = JSON.parse(pointsList);
    var para = document.createElement("p");
    var node = document.createTextNode(points[0]);
    para.appendChild(node);

    var element = document.getElementById("div1");
    element.appendChild(para);
    animate();
}

function animate(){
	var canvas = document.getElementById("myCanvas");
    var ctx = canvas.getContext("2d");
	
	var img = document.createElement("img");
	img.src = "animation.bmp";

	moveTo(ctx, img, 1);
}

function moveTo(ctx, img, i){
	ctx.drawImage(img, parseInt(points[i]), parseInt(points[i+1]));
	ctx.stroke();
	i += 2;
	if(i < parseInt(points[0]))
	{
		setTimeout(moveTo, 60, ctx, img, i);
	}
}