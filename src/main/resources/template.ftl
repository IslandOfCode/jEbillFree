<!DOCTYPE html>
<html>
	<head>
		<style>
			* {
				font-family: Arial;
				font-size: 8pt;
			}
			h1 {
				font-size: 2em;
			}
			div#nomefile {
				position: fixed;
				top: 1em;
				right: 5em;
				
				font-size: 10pt;
			}
			div#intestazione {
				width: 100%
				margin: auto;
			}
			div#emettitore {
				width: 45%;
				min-height: 100px;
				float: left;
				padding: 2px;
				border: solid 2px #a6a6a6;
			}
			div#destinatario {
				margin-left: 55%;
				min-height: 100px;
				padding: 2px;
				border: solid 2px #a6a6a6;
			}
			div.titoloBlocco {
				text-align:center;
				font-weight: bold;
				width: 100%;
				height: 8%;
				padding-top: 5px;
				padding-bottom: 5px;
				background: #a6a6a6;
			}
			
			table.common {
				width: 100%;
			}
			table, th, td{
				border: solid 2px #a6a6a6;
				border-collapse: collapse;
			}
			th {
				background-color: #a6a6a6;
			}
			
			table#total{
				float: right;
				width: 40%;
			}
			table#total th {
				width: 40%;
			}
			
			
			.grigio {
				color: #a6a6a6;/*lightgrey;*/
			}
			.destra {
				text-align: right;
			}
		</style>
	</head>
	<body>
		<div id="nomefile">Fattura elettronica n. ${NOMEFILE}</div>
		<br/><br/>
		<h1>${TipoDocumento} n. ${Numero}</h1>
		<br/><br/>
		<div id="intestazione">
			
			<div id="emettitore">
				<div class="titoloBlocco">
					Emettitore
				</div>
				${CPDenominazione}<br/>
				${CPIndirizzo}<br/>
				${CPCAP} ${CPComune} (${CPProvincia}) - ${CPNazione}<br/>
				<br/>
				<span class="grigio">P.IVA</span> ${CPIdPaese} ${CPIdCodice}<br/>
				<span class="grigio">C.F.</span> ${CPCodFiscale}<br/>
			</div>
			
			<div id="destinatario">
				<div class="titoloBlocco">
					Destinatario
				</div>
				${CCDenominazione}<br/>
				${CCIndirizzo}<br/>
				${CCCAP} ${CCComune} (${CCProvincia}) - ${CCNazione}<br/>
				<br/>
				<span class="grigio">P.IVA</span> ${CCIdPaese} ${CCIdCodice}<br/>
				<span class="grigio">C.F.</span> ${CCCodFiscale}<br/>
				<br/>
				<span class="grigio">Codice IPA</span> ${CodiceDestinatario}<br/>
				<span class="grigio">PEC</span> ${PECDestinatario}
			</div>

			<br/>
			<span class="grigio">Dati generali</span>
			
			<table id="generaldata" class="common">
				<tr>
					<th>Tipo Documento</th>
					<th>Data</th>
					<th>Numero</th>
					<th>Divisa</th>
					<!--<th>Importo Totale</th>-->
					<th>Arrotondamento</th>
					<th>Bollo Virtuale</th>
					<th>Importo Bollo</th>
				</tr>
				<tr>
					<td>${TipoDocumento}</td>
					<td class="destra">${Data}</td>
					<td class="destra">${Numero}</td>
					<td class="destra">${Divisa}</td>
					<!--<td class="destra">${ImportoTotaleDocumento}</td>-->
					<td class="destra">${Arrotondamento}</td>
					<td class="destra">${BolloVirtuale}</td>
					<td class="destra">${ImportoBollo}</td>
				</tr>
				<tr>
					<th colspan="8" style="text-align:left">Causale</th>
				</tr>
				<tr>
					<td colspan="8">${Causale}</td>
				</tr>
			</table>
			
			<#if DOAs?has_content || DDTs?has_content >
			<br/>
			<span class="grigio">Riferimenti ad altri documenti</span>
			
			<table id="otherdocument" class="common">
				<tr>
					<th>Tipo Riferimento</th>
					<th>Rif.Riga</th>
					<th>Numero Riferimento</th>
					<th>Data Riferimento</th>
					<th>Codice CUP</th>
					<th>Codice CIG</th>
				</tr>
				<#if DOAs?has_content>
					<#list DOAs as DOA>
						<tr>
							<td>${DatiOrdineAcquisto}</td>
							<td class="destra">${DOA.rifRiga!""}</td>
							<td class="destra">${DOA.idDocumento!""}</td>
							<td class="destra">${DOA.data!""}</td>
							<td class="destra">${DOA.cup!""}</td>
							<td class="destra">${DOA.cig!""}</td>
						</tr>
					</#list>
				</#if>
				<#if DDTs?has_content>
					<#list DDTs as DDT>
						<tr>
							<td>${DatiDDT}</td>
							<td class="destra">${DDT.rifRiga!""}</td>
							<td class="destra">${DDT.idDocumento!""}</td>
							<td class="destra">${DDT.data!""}</td>
							<td class="destra">${EMPTY}</td>
							<td class="destra">${EMPTY}</td>
						</tr>
					</#list>
				</#if>
			</table>
			</#if>

			<br/>
			<span class="grigio">Dettaglio per Riga Fattura</span>
			
			<table id="invoicerows" class="common">
				<tr>
					<th>Riga</th>
					<th>Cod</th>
					<th>Valore</th>
					<th>Descrizione</th>
					<th>Q.ta</th>
					<th>UM</th>
					<th>Prezzo</th>
					<th>sc/mg</th>
					<th>%</th>
					<!--<th>Val</th>-->
					<th>Importo</th>
					<th>IVA%</th>
				</tr>
				<!-- ATTENZIONE NON USARE INIZIALE MAIUSCOLA
					Quando si va a creare il bean, non usare l'iniziale maiuscola,
					forse perche' freemarker cerca i metodi get/set mettendo automaticamente
					l'iniziale maiuscola.
					Se questa è già maiusc, forse va nel panico e crede che non esista!
				  -->
				<#list righefattura as dettaglio>
					<tr>
						<td>${dettaglio.numeroLinea!""}</td>
						<td>${dettaglio.codice!""}</td>
						<td>${dettaglio.valore!""}</td>
						<td class="destra">${dettaglio.descrizione!""}</td>
						<td class="destra">${dettaglio.quantita!""}</td>
						<td class="destra">${dettaglio.unitaMisura!""}</td>
						<td class="destra">${dettaglio.prezzoUnitario!""}</td>
						<td class="destra">${dettaglio.scontoMaggiorazione!""}</td>
						<td class="destra">${dettaglio.ritenuta!""}</td>
						<!--<td class="destra">${dettaglio.val!""}</td>-->
						<td class="destra">${dettaglio.prezzoTotale!""}</td>
						<td class="destra">${dettaglio.aliquotaIVA!""}</td>
					</tr>
				</#list>
				
			</table>
			
			<br/>
			<span class="grigio">Riepilogo generale</span>
			
			<table id="recap" class="common">
				<tr>
					<th>Imponibile</th>
					<th>IVA %</th>
					<th>Imposta</th>
					<th>Natura</th>
					<th>Riferimento Normativo</th>
					<th>Esigibilita</th>
				</tr>
				<tr>
					<td>${ImponibileImporto}</td>
					<td>${AliquotaIVA}</td>
					<td>${Imposta}</td>
					<td>${Natura}</td>
					<td>${RiferimentoNormativo}</td>
					<td>${Esigibilita}</td>
				</tr>
			</table>
	
		<#if Beneficiario?has_content || Importo?has_content || Modalita?has_content>		
			<br/>
			<span class="grigio">Dati pagamento</span>
			
			<table id="payment" class="common">
				<tr>
					<th>Beneficiario</th>
					<th>Tipo</th>
					<th>Modalit&agrave;</th>
					<th>Data Termine</th>
					<th>Giorni pagamento</th>
					<th>Data Scadenza</th>
					<th>Importo</th>
				</tr>
				<tr>
					<td>${Beneficiario}</td>
					<td>${Tipo}</td>
					<td>${Modalita}</td>
					<td>${DataTermine}</td>
					<td>${GiorniPagamento}</td>
					<td>${DataScadenza}</td>
					<td>${Importo}</td>
				</tr>
			</table>
		</#if>
			
			<br/><br/><br/>
			<span class="grigio">Dati pagamento</span>
			
			<table id="total">
				<tr>
					<th>Totale Imponibile</th>
					<td>&euro; ${ImponibileImporto}</td>
				</tr>
				<tr>
					<th>Totale Imposte</th>
					<td>&euro; ${Imposta}</td>
				</tr>
				<tr>
					<th>Totale Esente</th>
					<td>&euro; ${Esente}</td>
				</tr>
				<tr>
					<th>Totale</th>
					<td>&euro; ${ImportoTotaleDocumento}</td>
				</tr>
			</table>
			
			
		</div>
		
		
	</body>
</html>