function swapImage(imageName, type)  {
  var newImage = "";

  if (type == "on")  {
    newImage = imageName + "-on";
  }
  else  {
    newImage = imageName + "-off";
  }

  if (document.images)  {
    document.images[imageName].src = "../images/" + newImage + ".gif";
  }
}
