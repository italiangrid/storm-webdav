const fileKeyLocalStorage = "__cart";
const requestIdLocalStorage = "__ids";
const mockStageUrl = "http://localhost:8086/mock/stage/";

const dateDifference = (date) => {
  const date_ = new Date(date);
  const date_now = new Date();
  const msDifference = Math.abs(date_now - date_);
  const daysDifference = Math.floor(msDifference / (1000 * 60 * 60 * 24));
  const hoursDifference = Math.floor((msDifference % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minDifference = Math.floor((msDifference % (1000 * 60 * 60)) / (1000 * 60));
  const secDifference = Math.floor((msDifference % (1000 * 60 * 60)) / 1000);
  if (daysDifference > 0) {
    return daysDifference + ' days ago';
  } else if (hoursDifference > 0) {
    return hoursDifference + ' hours ago';
  } else if (minDifference > 0) {
    return minDifference + ' min ago';
  } else if (secDifference > 0) {
    return secDifference + ' sec ago';
  }
}

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
    console.log("Reactivating " + buttonId)
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
  if (localStorage.getItem(fileKeyLocalStorage) === null) {
    localStorage.setItem(fileKeyLocalStorage, '[' + JSON.stringify(newFile) + ']');
  } else if (!JSON.parse(localStorage.getItem(fileKeyLocalStorage)).find(element => element.path === newFile.path)) {
    const tempList = JSON.parse(localStorage.getItem(fileKeyLocalStorage));
    tempList.push(newFile);
    localStorage.setItem(fileKeyLocalStorage, JSON.stringify(tempList));
  }
  showCartTable();
  disableButton(newFile.encodedId);
}

const removeFileToCart = (toRemoveFile) => {
  if (JSON.parse(localStorage.getItem(fileKeyLocalStorage)).find(element => element.path === toRemoveFile.path)) {
    let tempList = JSON.parse(localStorage.getItem(fileKeyLocalStorage));
    tempList = tempList.filter(element => element.path !== toRemoveFile.path);
    localStorage.setItem(fileKeyLocalStorage, JSON.stringify(tempList));
  } else {
    console.log("unable to remove " + toRemoveFile.path + ". This file isn't present in localStorage");
  }
  showCartTable();
  reactivateButton(toRemoveFile.encodedId);
}

const clearCart = () => {
  localStorage.removeItem(fileKeyLocalStorage);
  showCartTable();
  reactivateAllAddToQueueButtons();
}

const showCartTable = () => {
  let cartRowHTML = "";
  let badgeCounter;

  if (localStorage.getItem(fileKeyLocalStorage)) {
    const fileCart = JSON.parse(localStorage.getItem(fileKeyLocalStorage));
    badgeCounter = fileCart.length;
    fileCart.forEach((item) => {
      cartRowHTML +=
        "<tr>" +
        '<td>' + '<a href=' + item.path + '>' + item.path + '</a>' + '</td>' +
        '<td>' +
        '<button type="button" class="btn btn-outline-danger btn-sm"' +
        'onclick="removeFileToCart({path: \'' + item.path + '\' })">' +
        '<span aria-hidden="true">&times; Remove</span>' +
        '</button>'
        + "</td>" +
        "</tr>";
    });
  } else {
    badgeCounter = 0;
  }

  $('.modal-cart-table-body').html(cartRowHTML);
  $('.cart-badge').text(badgeCounter);
}

const renderMonitoringRowHTML = (responseObject) => {
  let monitoringItem = '<div class="accordion-item">' +
    '<h2 class="accordion-header" id="heading' + responseObject.id + '">' +
    '<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" ' +
    'data-bs-target="#collapse' + responseObject.id + '" aria-expanded="false" ' +
    'aria-controls="collapse' + responseObject.id + '">' +
    '<span class="accordion-button-content-id">&nbsp;&nbsp;&nbsp;' + responseObject.id + '</span>' +
    '<span class="accordion-button-content-date"> ' + dateDifference(responseObject.createdAt) + '</span>' +
    '<span class="accordion-button-content-date"> ' + dateDifference(responseObject.startedAt) + '</span>' +
    '</button>' + '</h2>' +
    '<div id="collapse' + responseObject.id + '" class="accordion-collapse collapse"' +
    'aria-labelledby="heading' + responseObject.id + '" data-bs-parent="#accordionMonitoring">' +
    '<div class="accordion-body">' +

    '<table class="table table-borderless">' +
    '<thead>' +
    '<tr>' +
    '<th scope="col"></th>' +
    '<th scope="col">File path</th>' +
    '<th scope="col">Status</th>' +
    '</tr>' +
    '</thead>' +
    '<tbody>';

  let i = 1;
  responseObject.files.forEach((file) => {
    if (file.status === 'COMPLETED') {
      monitoringItem +=
        '<tr>' +
        '<th style="text-align: center">' + i + '</th>' +
        '<td style="background-color: rgba(0, 128, 0, 0.131); color: green;">' + file.path + '</td>' +
        '<td style="background-color: rgba(0, 128, 0, 0.131); color: green;">' + file.status + '</td>' +
        '</tr>';
      ++i;
    } else if (file.status === 'FAILED') {
      monitoringItem +=
        '<tr>' +
        '<th style="text-align: center">' + i + '</th>' +
        '<td style="background-color: rgba(255, 0, 0, 0.221); color: red;">' + file.path + '</td>' +
        '<td style="background-color: rgba(255, 0, 0, 0.221); color: red;">' + file.status + '</td>' +
        '</tr>';
      ++i;
    } else if (file.status === 'CANCELLED') {
      monitoringItem +=
        '<tr>' +
        '<th style="text-align: center">' + i + '</th>' +
        '<td style="background-color: rgba(128, 128, 128, 0.219); color: gray;">' + file.path + '</td>' +
        '<td style="background-color: rgba(128, 128, 128, 0.219); color: gray;">' + file.status + '</td>' +
        '</tr>';
      ++i;
    } else {
      monitoringItem +=
        '<tr>' +
        '<th style="text-align: center">' +
        i +
        '</th>' +
        '<td>' +
        '<div class="d-flex justify-content-between">' +
        file.path +
        '<button type="button" class="btn btn-outline-danger btn-sm tex">' +
        '<span aria-hidden="true">&times; Cancel</span>' +
        '</button>' +
        '</div>' +
        '</td>' +
        '<td>' + file.status + '</td>' +
        '</tr>';
      ++i;
    }
  });

  monitoringItem += '</tbody>' + '</table>' + '</div>' + '</div>' + '</div>';
  return monitoringItem;
}

const renderMonitoringRowError = (response) => {
  return ('<div class="accordion-item">' +
    '<h2 class="accordion-header">' +
    '<button class="accordion-button accordion-button-error collapsed" type="button" disabled>' +
    '<i class="bi bi-x-circle"></i>&nbsp;' + response +
    '</button>' + '</h2>');
}

const manageMonitoring = () => {
  const setWithExpiry = (key, value, expireTimeInMilliseconds) => {
    const now = new Date();
    const objectToSet = {
      value: value,
      expiry: now.getTime() + expireTimeInMilliseconds
    };
    localStorage.setItem(key, JSON.stringify(objectToSet));
  }

  const removeFromIdList = (idToRemove) => {
    const idListStr = localStorage.getItem(requestIdLocalStorage);
    const newIdList = JSON.parse(idListStr).filter(id => id.requestId != idToRemove);
    console.log(idToRemove);
    localStorage.setItem(requestIdLocalStorage, JSON.stringify(newIdList));
  }

  const getWithExpiry = (key) => {
    const objectStr = localStorage.getItem(key);
    if (!objectStr) {
      return null;
    }
    const object = JSON.parse(objectStr);
    if (object.expiry) {
      const now = new Date();
      if (now.getTime() > object.expiry) {
        localStorage.removeItem(key);
        removeFromIdList(key);
        return null;
      }
      return object.value;
    } else {
      return object;
    }
  }

  if (localStorage.getItem(requestIdLocalStorage)) {
    const requestIds = JSON.parse(localStorage.getItem(requestIdLocalStorage));
    let monitoringRowHTML = '';
    $('#accordionMonitoring').empty();
    requestIds.forEach((id) => {
      $.get(mockStageUrl + id.requestId)
        .then((data) => {
          const responseInLS = getWithExpiry(id.requestId);
          const filesStatus = data.files.map(file => file.status);
          if (responseInLS === null) {
            localStorage.setItem(id.requestId, JSON.stringify(data));
            monitoringRowHTML = renderMonitoringRowHTML(data);
          } else if (filesStatus.every(fs => fs === 'COMPLETED')) {
            setWithExpiry(data.id, data, 86400000); // 24 hours
            monitoringRowHTML = renderMonitoringRowHTML(data);
          } else {
            localStorage.setItem(id.requestId, JSON.stringify(data));
            monitoringRowHTML = renderMonitoringRowHTML(data);
          }
          $('#accordionMonitoring').append(monitoringRowHTML);
        })
        .catch((error) => {
          $('#accordionMonitoring').append(renderMonitoringRowError(error.responseText));
        });
    });
  }
}

const makePost = () => {
  const cartFilePaths = { files: JSON.parse(localStorage.getItem(fileKeyLocalStorage)).map((item) => { return { path: item.path }; }) };
  console.log(JSON.stringify(cartFilePaths));
  $.ajax({
    type: "POST",
    url: mockStageUrl,
    contentType: "application/json; charset=utf-8",
    dataType: "json",
    data: JSON.stringify(cartFilePaths)
  })
    .done(
      (data, status) => {
        alert("Data: " + JSON.stringify(data) + "\nStatus: " + status);
        if (localStorage.getItem(requestIdLocalStorage) === null) {
          localStorage.setItem(requestIdLocalStorage, '[' + JSON.stringify(data) + ']');
        } else {
          const tempList = JSON.parse(localStorage.getItem(requestIdLocalStorage));
          tempList.push(data);
          localStorage.setItem(requestIdLocalStorage, JSON.stringify(tempList));
        }
        // manageMonitoring();
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

  $(document).ready(() => {
    $('#searchInput').on('input', function () {
      var searchText = $(this).val().toLowerCase();
      $('.jd-row').each(function () {
        var rowData = $(this).text().toLowerCase();
        if (rowData.indexOf(searchText) === -1) {
          $(this).hide();
        } else {
          $(this).show();
        }
      });
    });
  });
});
