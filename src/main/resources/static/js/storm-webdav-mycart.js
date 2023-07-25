const keyLocalStorage = "__files";

const encodedString = (stringToEncode) => {
  return CryptoJS.SHA256(stringToEncode).toString(CryptoJS.enc.Hex);
}

const disableButton = (buttonId) => {
  try {
    $('#' + buttonId).prop('disabled', true);
  } catch {
    console.log("Error in the disabilitation of the button " + buttonId);
  }
}

const reactivateButton = (buttonId) => {
  try {
    $('#' + buttonId).prop('disabled', false);
  } catch {
    console.log("Error in the reactivation of the button " + buttonId);
  }
}

const reactivateAllAddToQueueButtons = () => {
  $('.add-to-queue-button').prop('disabled', false);
}

const addFileToCart = (newFile) => {
  newFile.encodedId = CryptoJS.SHA256(newFile.path).toString(CryptoJS.enc.Hex);
  if (localStorage.getItem(keyLocalStorage) === null) {
    localStorage.setItem(keyLocalStorage, '[' + JSON.stringify(newFile) + ']');
  } else if (!JSON.parse(localStorage.getItem(keyLocalStorage)).find(element => element.path === newFile.path)) {
    const tempList = JSON.parse(localStorage.getItem(keyLocalStorage));
    tempList.push(newFile);
    localStorage.setItem(keyLocalStorage, JSON.stringify(tempList));
  }
  showCartTable();
  disableButton(newFile.encodedId);
}

const removeFileToCart = (toRemoveFile, buttonId) => {
  if (JSON.parse(localStorage.getItem(keyLocalStorage)).find(element => element.path === toRemoveFile.path)) {
    let tempList = JSON.parse(localStorage.getItem(keyLocalStorage));
    tempList = tempList.filter(element => element.path !== toRemoveFile.path);
    localStorage.setItem(keyLocalStorage, JSON.stringify(tempList));
  } else {
    console.log("unable to remove " + toRemoveFile.path + ". This file isn't present in localStorage");
  }
  showCartTable();
  reactivateButton(buttonId);
}

const clearCart = () => {
  localStorage.removeItem(keyLocalStorage);
  showCartTable();
  reactivateAllAddToQueueButtons();
}

const showCartTable = () => {
  let cartRowHTML = "";
  let badgeCounter;
  if (localStorage.getItem(keyLocalStorage)) {
    const fileCart = JSON.parse(localStorage.getItem(keyLocalStorage));
    badgeCounter = fileCart.length;
    fileCart.forEach((item) => {
      cartRowHTML +=
        "<tr>" +
        '<td>' + '<a href=' + item.path + '>' + item.name + '</a>' + '</td>' +
        '<td>' + item.sizeInBytes + '</td>' +
        '<td>' +
        '<button type="button" class="btn btn-outline-danger btn-sm"' +
        'onclick="removeFileToCart({path: \'' + item.path + '\' }, \'' + item.encodedId + '\')">' +
        '<span aria-hidden="true">&times; Remove</span>' +
        '</button>'
        + "</td>" +
        "</tr>";
    });
  } else {
    badgeCounter = 0;
  }

  $('.modal-table-body').html(cartRowHTML);
  $('.cart-badge').text(badgeCounter);
}

const makePost = () => {
  const cartFilePaths = { files: JSON.parse(localStorage.getItem(keyLocalStorage)).map((item) => { return { path: item.path }; }) };
  console.log(JSON.stringify(cartFilePaths));
  $.ajax({
    type: "POST",
    url: "http://localhost:8086/ciao",
    data: JSON.stringify(cartFilePaths)
  })
    .done(
      (data, status) => {
        alert("Data: " + data + "\nStatus: " + status);
        clearCart();
      }
    )
    .fail(
      (data) => {
        alert("Ajax failed: " + data['respondeText']);
      }
    )
}

$(document).ready(() => {
  showCartTable();
});
