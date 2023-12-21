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

	if ($("#fault_type").val() != null || $("#fault_type").val() != "") {
		var faultTypes = $("#fault_type").val();

		var faultTypesArray = faultTypes.split(',');

		$(".circle").each(function() {
			var circleId = $(this).attr("id");
			var circleType = getCircleType(circleId);

			if (faultTypesArray.includes(circleType)) {
				$(this).css("background-color", "red");
			}
		});
		
		$("#pdf1").css("display", "inline");
		$("#pdf2").css("display", "inline");
	}
});

function eliminarItem(item) {
	//window.alert(item);
	if (item != null) {
		jQuery.ajax({
			url: '/deleteDataset',
			type: 'POST',
			data: { item: item },
			success: function() {
				location.reload();
			},
			error: function(error) {
				console.error(error);
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

function getCircleType(circleId) {
	switch (circleId) {
		case "colorCircle1":
			return "Outer-race";
		case "colorCircle2":
			return "Inner-race";
		case "colorCircle3":
			return "Bearing balls";
		case "colorCircle4":
			return "Cage";
		default:
			return "";
	}
}
