<link rel="stylesheet" href="https://cdn.datatables.net/1.10.19/css/jquery.dataTables.min.css"></link>
<script src="https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/plug-ins/1.10.19/dataRender/datetime.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/moment.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/locale/ja.js"></script>

<div id="contentarea">
<div id="content-messages"></div>
</div
<div id="payments" title="Payment Schedule">
  <div>
    <label>${uiLabelMap.FormFieldTitle_quoteId}</label>
    <input type="text" id="quoteId" value="${quoteId}"></input>
  </div>
  <div>
    <label>${uiLabelMap.OrderPrice}</label>
    <input type="text" id="price"></input>
  </div>
  <div>
    <label>${uiLabelMap.OrderOrderQuoteEstimatedDeliveryDate}</label>
    <@htmlTemplate.renderDateTimeField id="deliveryDate" name="deliveryDate" event="" action="" value="" className="" alert="" title="Format: yyyy-MM-dd" size="25" maxlength="30" id="itemDesiredDeliveryDate1" dateType="date" shortDateInput=true timeDropdownParamName="" defaultDateTimeString="" localizedIconTitle="" timeDropdown="" timeHourName="" classString="" hour1="" hour2="" timeMinutesName="" minutes="" isTwelveHour="" ampmName="" amSelected="" pmSelected="" compositeType="" formName=""/>
  </div>
  <div>
    <label>${uiLabelMap.CommonPeriod}</label>
    <input type="text" id="term"></input>
  </div>
  <div>
    <label>${uiLabelMap.QuoteLeasePaymentTimes}</label>
    <input type="text" id="paymentTimes"></input>
  </div>
  <div>
    <label>${uiLabelMap.QuoteLeasePerYearRate}</label>
    <input type="text" id="paymentRate"></input>
  </div>
  <!-- div>
    <label>${uiLabelMap.QuoteLeaseRate}</label>
    <input type="text" id="leaseRate"></input>
  </div -->
  <div>
    <input type="button" id="submit" value="${uiLabelMap.CommonSubmit}">
  </div>

  <table id="paymentSchedule" class="display" style="width:100%">
    <thead>
      <tr><th>#</th><th>${uiLabelMap.CommonDate}</th><th>${uiLabelMap.QuoteLeaseAmount}</th><th>${uiLabelMap.QuoteLeaseCost}</th><th>${uiLabelMap.QuoteLeaseInterest}</th><th>${uiLabelMap.QuoteLeaseRemain}</th></tr>
    </thead>
    <tbody>
    </tfoot>
  </table>

</div>

<script type="text/javascript">
	$(document).ready( function () {
	    $.ajax({
	        url: 'getQuotePayment',
	        type: "POST",
	        data:{
	        	quoteId: '${quoteId}'
	        },
	        success: function(res) {
	            console.log(res);

				$('#price').val(res.price);
//				$('input[name="deliveryDate_i18n"]').val(res.paymentDay);
				$('input[name="deliveryDate"]').val(res.paymentDay).change();
				$('#term').val(res.paymentTimes);
				$('#paymentTimes').val(res.paymentTimes);
				$('#paymentRate').val(res.paymentRate);
				$('#leaseRate').val(res.leaseRate);

				table = $('#paymentSchedule').DataTable();
				table.destroy();
				table = $('#paymentSchedule')
					.addClass( 'nowrap' )
					.dataTable( {
						responsive: true,
						columnDefs: [
							{ targets: 1, render: $.fn.dataTable.render.moment( 'x', 'YYYY-MM-DD', 'en' ) },
							{ targets: [-1, -2, -3, -4], className: 'dt-body-right' }
						],
						data: res.lists,
						language: {
							url: '//cdn.datatables.net/plug-ins/1.10.19/i18n/Japanese.json'
						},
						searching: false
					} )

	        }
	    });

//		$('#paymentSchedule')
//			.addClass( 'nowrap' )
//			.dataTable( {
//				responsive: true,
//				columnDefs: [
//					{ targets: [-1, -2, -3, -4], className: 'dt-body-right' }
//				]
//			} );
		$('#submit').on('click', function() {
		    $.ajax({
		        url: 'createQuotePayment',
		        type: "POST",
		        data:{
		        	quoteId: '${quoteId}',
		        	paymentDay: $('input[name="deliveryDate"]').val(),
		        	price: $('#price').val(),
		        	paymentTimes: $('#paymentTimes').val(),
		        	paymentRate: $('#paymentRate').val(),
		        	leaseRate: $('#leaseRate').val()
		        },
		        success: function(res) {
		            console.log(res);

					if (res._ERROR_MESSAGE_) {
						jQuery('#content-messages').addClass('errorMessage');
						jQuery('#content-messages' ).html(res._ERROR_MESSAGE_);
						showjGrowl();
//						showErrorAlert('ERROR', res._ERROR_MESSAGE_);
						return false;
					}

					table = $('#paymentSchedule').DataTable();
					table.destroy();
					table = $('#paymentSchedule')
						.addClass( 'nowrap' )
						.dataTable( {
							responsive: true,
							columnDefs: [
								{ targets: 1, render: $.fn.dataTable.render.moment( 'x', 'YYYY-MM-DD', 'en' ) },
								{ targets: [-1, -2, -3, -4], className: 'dt-body-right' }
							],
							data: res.lists,
							language: {
								url: '//cdn.datatables.net/plug-ins/1.10.19/i18n/Japanese.json'
							},
							searching: false
						} )

		        }
		    });
		});
	} );
</script>
