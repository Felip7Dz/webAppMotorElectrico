$(document).ready(function() {
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

	$('#submitButton').prop('disabled', true);
	$('.single-checkbox').change(function() {
		$('.single-checkbox').not(this).prop('checked', false);

		var alMenosUnaSeleccionada = $('.single-checkbox:checked').length > 0;
		$('#submitButton').prop('disabled', !alMenosUnaSeleccionada);
	});

	$('#formDataset').submit(function() {
		if ($('.single-checkbox:checked').length > 0) {
			return true;
		} else {
			$('#myModal').modal('show');
			return false;
		}
	});

	$('#savedSubmitButton').prop('disabled', true);
	$('.saved-single-checkbox').change(function() {
		$('.saved-single-checkbox').not(this).prop('checked', false);

		var alMenosUnaSeleccionada = $('.saved-single-checkbox:checked').length > 0;
		$('#savedSubmitButton').prop('disabled', !alMenosUnaSeleccionada);
	});

	$('#formSavedDataset').submit(function() {
		if ($('.saved-single-checkbox:checked').length > 0) {
			return true;
		} else {
			$('#myModal').modal('show');
			return false;
		}
	});

	$(".onlyNums").on("input", function(e) {
		var currentValue = $(this).val();

		var newValue = currentValue.replace(/[^0-9.,]/g, '');

		$(this).val(newValue);
	});

	if ($("#analysis_result").val() != null && $("#analysis_result").val() != "") {
		var faultTypes = $("#fault_type").val();

		var faultTypesArray = faultTypes.split(',');

		$(".circle").each(function() {
			var circleId = $(this).attr("id");
			var circleType = getCircleType(circleId);

			if (faultTypesArray.includes(circleType)) {
				$(this).css("background-color", "red");
			}
		});

		if ($("#fault_detected").val() == "true") {
			$("#pdf1").css("display", "inline");
			$("#pdf2").css("display", "inline");
		}

		$("#imgph1").hide();
		$("#imgph2").hide();

		$("#n_healthy_data").val($("#n_healthy_used").val());
	}
	
	if($("#files_added").val() == 1){
		$("#formNewUpload").hide();
		$("#formDataCheckNew").show();
		$("#runNew").show();
	}
	if($("#files_added").val() == 0){
		$("#formNewUpload").show();
		$("#formDataCheckNew").hide();
		$("#runNew").hide();
	}
	
	if($("#warningsDivNew").is(":visible")){
		$("#formNewUpload").hide();
		$("#formDataCheckNew").hide();
	}
	
	$("#id2send").val($("#id").val());
	$("#name2send").val($("#nombre").val());

});

function eliminarItem(item) {
	if (item != null) {
		jQuery.ajax({
			url: '/deleteDataset',
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
            url: '/deleteSample',
            type: 'POST',
            data: { nombre: nombre, id: id },
            success: function() {
                location.reload();
            }
        });
    }
}

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

	$("#nombre_req").val(nombre);
	$("#sampling_frequency_req").val(samplingFrequency);
	$("#bpfo_req").val(bpfo);
	$("#bpfi_req").val(bpfi);
	$("#bsf_req").val(bsf);
	$("#ftf_req").val(ftf);
	$("#first_sample_req").val(firstSample);
	$("#analyzed_number_req").val(analyzedNumber - firstSample);
	$("#healthy_number_req").val(n_healthy);
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

	$("#nombre_req").val(nombre);
	$("#sampling_frequency_req").val(samplingFrequency);
	$("#bpfo_req").val(bpfo);
	$("#bpfi_req").val(bpfi);
	$("#bsf_req").val(bsf);
	$("#ftf_req").val(ftf);
	$("#first_sample_req").val(firstSample);
	$("#analyzed_number_req").val(analyzedNumber - firstSample);
	$("#healthy_number_req").val(n_healthy);
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

$("#min_to_check").click(function(){
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
