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

	$("#nombre_req").val(nombre);
	$("#sampling_frequency_req").val(samplingFrequency);
	$("#bpfo_req").val(bpfo);
	$("#bpfi_req").val(bpfi);
	$("#bsf_req").val(bsf);
	$("#ftf_req").val(ftf);
	$("#first_sample_req").val(firstSample);
	$("#analyzed_number_req").val(analyzedNumber);
});
