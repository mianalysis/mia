function copyCode(currButton, currElement) {
  // Getting the active code
  var currText = document.getElementById(currElement).querySelector(".active").textContent.trim();

  // Copy the text inside the text field
  navigator.clipboard.writeText(currText);

  // Alert the copied text
  document.getElementById(currButton).textContent = "Copied";
  setTimeout(() => document.getElementById(currButton).textContent = "Copy", 2000);
}