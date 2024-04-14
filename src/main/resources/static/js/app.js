$(document).ready(function() {
	
	if($("#loggedUserFlag").val() != "admin"){
		$("#adminUsersBtt").hide();
	}
	
	const dropContainer = $("#dropcontainer");
	const fileInput = $("#file");

	dropContainer.on("dragover", function(e) {
		e.preventDefault();
	});

	dropContainer.on("dragenter", function() {
		dropContainer.addClass("drag-active");
	});

	dropContainer.on("dragleave", function() {
		dropContainer.removeClass("drag-active");
	});

	dropContainer.on("drop", function(e) {
		e.preventDefault();
		dropContainer.removeClass("drag-active");
		fileInput.prop("files", e.originalEvent.dataTransfer.files);
	});

	$('.single-checkbox').click(function() {
		$('.single-checkbox').prop('checked', false);
		$(this).prop('checked', true);
	});

	$('#submitButton').click(function() {
		var selectedCheckbox = $('.single-checkbox:checked');
		var formToSubmit = selectedCheckbox.closest('form');

		if (selectedCheckbox.length === 1) {
			formToSubmit.submit();
		} else {
			$('#myModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
		}
	});


	$(".onlyNums").on("input", function(e) {
		var currentValue = $(this).val();

		var newValue = currentValue.replace(/[^0-9.,]/g, '');

		$(this).val(newValue);
	});

	if ($("#fault_detected").val() == 'true') {
		var faultTypes = $("#fault_type").val();

		var faultTypesArray = faultTypes.split(',');
		
		if ($("#fault_info").val() == 'A fault has been detected in an early stage') {
			$(".circle").each(function() {
				var circleId = $(this).attr("id");
				var circleType = getCircleType(circleId);

				if (faultTypesArray.includes(circleType)) {
					$(this).css("background-color", "yellow");
				}
			});
		}
		if ($("#fault_info").val() == 'A fault has been detected in a medium stage') {
			$(".circle").each(function() {
				var circleId = $(this).attr("id");
				var circleType = getCircleType(circleId);

				if (faultTypesArray.includes(circleType)) {
					$(this).css("background-color", "orange");
				}
			});
		}
		if ($("#fault_info").val() == 'A fault has been detected in a last degradation stage') {
			$(".circle").each(function() {
				var circleId = $(this).attr("id");
				var circleType = getCircleType(circleId);

				if (faultTypesArray.includes(circleType)) {
					$(this).css("background-color", "red");
				}
			});
		}
		
		$("#pdf1").css("display", "inline");
		$("#pdf2").css("display", "inline");
		$("#imgph1").hide();
		$("#imgph2").hide();
		$("#n_healthy_data").val($("#n_healthy_used").val());
	}

	if ($("#fault_detected").val() == 'false') {
		$("#imgph1").hide();
		$("#imgph2").hide();
		$("#n_healthy_data").val($("#n_healthy_used").val());
	}

	if ($("#files_added").val() == 1) {
		$("#formNewUpload").hide();
		$("#formDataCheckNew").show();
		$("#runNew").show();
	}
	if ($("#files_added").val() == 2) {
		$("#formNewUpload").hide();
		$("#allDataLoaded").hide();
		$("#formDataCheckNew").show();
		$("#onlyHealthyLoaded").show();
		$("#runNew").hide();
	}
	if ($("#files_added").val() == 0) {
		$("#formNewUpload").show();
		$("#formDataCheckNew").hide();
		$("#runNew").hide();
	}

	if ($("#warningsDivNew").is(":visible")) {
		$("#formNewUpload").hide();
		$("#formDataCheckNew").hide();
	}

	if ($("#warningsDivAPI").is(":visible")) {
		$("#saveDatasetBtt").hide();
		$("#manageLink").hide();
		$("#adminUsersBtt").hide();
		$("#submitButton").hide();
		$("#newDatasetBtt").hide();
	}

	$("#id2send").val($("#id").val());
	$("#name2send").val($("#nombre").val());

	if ($("#imgOk").is(":visible")) {
		$("#imgph1").hide();
		$("#imgph2").hide();
	}
});

$(document).on('keydown', function(event) {
    if ($('#loadingModal').is(':visible') && event.keyCode === 27) {
        event.preventDefault();
        event.stopPropagation();
    }
});

function eliminarUser(item) {
	if (item != null && item != "admin") {
		jQuery.ajax({
			url: '/webAppMotorElectrico/deleteUser',
			type: 'POST',
			data: { item: item },
			success: function() {
				location.reload();
			}
		});
	}
}

function eliminarItem(item) {
	if (item != null) {
		jQuery.ajax({
			url: '/webAppMotorElectrico/deleteDataset',
			type: 'POST',
			data: { item: item },
			success: function() {
				location.reload();
			}
		});
	}
}

function eliminarSample(nombre, id) {
	if (nombre != null && id != null) {
		jQuery.ajax({
			url: '/webAppMotorElectrico/deleteSample',
			type: 'POST',
			data: { nombre: nombre, id: id },
			success: function() {
				location.reload();
			}
		});
	}
}

$("#saveDatasetBtt").click(function() {
	$('#uploadingModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
	$("#formUpload").submit();
});

$("#uploadDataSamples").click(function() {
	$('#uploadingModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
	$("#formNewUpload").submit();
});

$("#saveDatasetInfoBtt").click(function() {
	if ($("#nombre").val() == "New Dataset") {
		event.preventDefault();
		$("#dataInfoNotFoundH4").text("You must change the name.");
		$("#warningsDivNew").css({ "background-color": "red", "border": "red" });
	}
});

$("#uploadDataSampleBtt").click(function() {
	if ($("#uploadDataSampleInput").val() == '') {
		event.preventDefault();
	} else {
		$("#name2send2").val($("#name2send").val());
		$("#id2send2").val($("#id2send").val());
		$('#uploadingModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
	}
});

$("#runPre").click(function() {
	var nombre = $("#nombre").val();
	var samplingFrequency = $("#sampling_frequency").val();
	var bpfo = $("#bpfo").val();
	var bpfi = $("#bpfi").val();
	var bsf = $("#bsf").val();
	var ftf = $("#ftf").val();
	var firstSample = $("#min_to_check").val();
	var analyzedNumber = $("#max_to_check").val();
	var n_healthy = $("#n_healthy_data").val();

	if (n_healthy != '' && firstSample != '' && analyzedNumber != '') {
		$("#nombre_req").val(nombre);
		$("#sampling_frequency_req").val(samplingFrequency);
		$("#bpfo_req").val(bpfo);
		$("#bpfi_req").val(bpfi);
		$("#bsf_req").val(bsf);
		$("#ftf_req").val(ftf);
		$("#first_sample_req").val(firstSample);
		$("#analyzed_number_req").val(analyzedNumber - firstSample);
		$("#healthy_number_req").val(n_healthy);

		$('#loadingModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
		
		$("#runPreform").submit();
	}else{
		location.reload();
	}
});

$("#runNew").click(function() {
	var nombre = $("#nombre").val();
	var samplingFrequency = $("#sampling_frequency").val();
	var bpfo = $("#bpfo").val();
	var bpfi = $("#bpfi").val();
	var bsf = $("#bsf").val();
	var ftf = $("#ftf").val();
	var firstSample = $("#min_to_check").val();
	var analyzedNumber = $("#max_to_check").val();
	var n_healthy = $("#n_healthy_data").val();

	if (n_healthy != '' && firstSample != '' && analyzedNumber != '') {
		$("#nombre_req").val(nombre);
		$("#sampling_frequency_req").val(samplingFrequency);
		$("#bpfo_req").val(bpfo);
		$("#bpfi_req").val(bpfi);
		$("#bsf_req").val(bsf);
		$("#ftf_req").val(ftf);
		$("#first_sample_req").val(firstSample);
		$("#analyzed_number_req").val(analyzedNumber - firstSample);
		$("#healthy_number_req").val(n_healthy);

		$('#loadingModal').modal({
            backdrop: 'static',
            keyboard: false 
        }).modal('show');
		
		$("#runNewform").submit();
	}else{
		location.reload();
	}
});

function getCircleType(circleId) {
	switch (circleId) {
		case "colorCircle1":
			return "Outer_race";
		case "colorCircle2":
			return "Inner_race";
		case "colorCircle3":
			return "Bearing_balls";
		case "colorCircle4":
			return "Cage";
		default:
			return "";
	}
}

$("#pdf1").click(function() {
	var fault_detected = $("#fault_detected").val();
	var fault_info = $("#fault_info").val();
	var fault_type = $("#fault_type").val();
	var fault_details = $("#fault_details").val();
	var analysis_result = $("#analysis_result").val();

	$.ajax({
		type: "GET",
		url: "/generate-pdf",
		data: {
			fault_detected: fault_detected,
			fault_info: fault_info,
			fault_type: fault_type,
			fault_details: fault_details,
			analysis_result: analysis_result
		},
		success: function(data) {
			var blob = b64toBlob(data, 'application/pdf');
			var link = document.createElement('a');
			link.href = window.URL.createObjectURL(blob);
			link.target = '_blank';
			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
		},
		error: function(error) {
			console.error("Error al generar el PDF: " + error);
		}
	});
});

$("#pdf2").click(function() {
	var fault_detected = $("#fault_detected").val();
	var fault_info = $("#fault_info").val();
	var fault_type = $("#fault_type").val();
	var fault_details = $("#fault_details").val();
	var analysis_result = $("#analysis_result").val();

	$.ajax({
		type: "GET",
		url: "/generate-pdf",
		data: {
			fault_detected: fault_detected,
			fault_info: fault_info,
			fault_type: fault_type,
			fault_details: fault_details,
			analysis_result: analysis_result
		},
		success: function(data) {
			var blob = b64toBlob(data, 'application/pdf');
			var link = document.createElement('a');
			link.href = window.URL.createObjectURL(blob);
			link.target = '_blank';
			document.body.appendChild(link);
			link.click();
			document.body.removeChild(link);
		},
		error: function(error) {
			console.error("Error al generar el PDF: " + error);
		}
	});
});

$("#min_to_check").click(function() {
	$(this).val('');
});

function b64toBlob(base64, contentType) {
	contentType = contentType || '';
	const sliceSize = 512;
	const byteCharacters = atob(base64);
	const byteArrays = [];

	for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
		const slice = byteCharacters.slice(offset, offset + sliceSize);
		const byteNumbers = new Array(slice.length);
		for (let i = 0; i < slice.length; i++) {
			byteNumbers[i] = slice.charCodeAt(i);
		}
		const byteArray = new Uint8Array(byteNumbers);
		byteArrays.push(byteArray);
	}

	return new Blob(byteArrays, { type: contentType });
}
