"""Generate MNP Port-In Integration Guide PDF."""
from fpdf import FPDF
from pathlib import Path

OUT = Path(__file__).parent / "MNP_PortIn_Integration_Guide.pdf"


class GuidePDF(FPDF):
    def header(self):
        self.set_font("Helvetica", "B", 10)
        self.cell(0, 8, "Lyca USA - MNP Port-In Integration Guide", align="C", new_x="LMARGIN", new_y="NEXT")
        self.ln(2)

    def footer(self):
        self.set_y(-15)
        self.set_font("Helvetica", "I", 8)
        self.cell(0, 10, f"Page {self.page_no()}", align="C")

    def section_title(self, title: str):
        self.set_font("Helvetica", "B", 14)
        self.set_fill_color(230, 230, 230)
        self.cell(0, 10, title, new_x="LMARGIN", new_y="NEXT", fill=True)
        self.ln(2)

    def sub_title(self, title: str):
        self.set_font("Helvetica", "B", 11)
        self.cell(0, 8, title, new_x="LMARGIN", new_y="NEXT")
        self.ln(1)

    def body_text(self, text: str):
        self.set_font("Helvetica", "", 10)
        self.multi_cell(0, 5, text)
        self.ln(2)

    def code_block(self, text: str):
        self.set_font("Courier", "", 8)
        self.set_fill_color(245, 245, 245)
        for line in text.split("\n"):
            self.cell(0, 4, line, new_x="LMARGIN", new_y="NEXT", fill=True)
        self.ln(3)

    def table_row(self, cols, widths, header=False):
        self.set_font("Helvetica", "B" if header else "", 9)
        for i, (col, w) in enumerate(zip(cols, widths)):
            self.cell(w, 7, col, border=1)
        self.ln()


def build_pdf():
    pdf = GuidePDF()
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.add_page()

    pdf.set_font("Helvetica", "B", 18)
    pdf.cell(0, 12, "MNP Port-In Integration Guide", new_x="LMARGIN", new_y="NEXT", align="C")
    pdf.set_font("Helvetica", "", 10)
    pdf.cell(0, 6, "Lyca USA | Plintron MNP Proxy Spec V1.1 + USA API Spec V1.3", new_x="LMARGIN", new_y="NEXT", align="C")
    pdf.cell(0, 6, "Generated: September 2026", new_x="LMARGIN", new_y="NEXT", align="C")
    pdf.ln(5)

    pdf.section_title("1. Overview")
    pdf.body_text(
        "This guide covers Mobile Number Portability (MNP) Port-In integration for Lyca USA using "
        "Plintron MNP Proxy APIs. Port-Out APIs are NOT documented in the provided specification PDFs.\n\n"
        "Integration flow:\n"
        "  Step 0 (optional): GET_CUSTOMER_DETAILS\n"
        "  Step 1: INITIATE_PORTIN_REQUEST\n"
        "  Step 2: CHECK_PORTIN_STATUS (poll until terminal state)\n"
        "  Step 3: GET_ACCOUNT_DETAILS (verify MNP_IND = 1 after completion)"
    )

    pdf.section_title("2. Environment Variables")
    pdf.body_text("Replace placeholders with values from your Plintron/Lyca integration pack:")
    pdf.code_block(
        "MNP_BASE_URL=https://<mnp-proxy-host>/<path>\n"
        "ITG_BASE_URL=https://<itg-gateway-host>/<path>\n"
        "AUTH_TOKEN=<your-bearer-or-basic-token>\n"
        "ENTITY=ITGSIM\n"
        "CHANNEL_REF=CCARE"
    )

    pdf.section_title("3. Common Headers (All MNP Calls)")
    widths = [55, 55, 80]
    pdf.table_row(["Header", "Value", "Notes"], widths, header=True)
    pdf.table_row(["Content-Type", "application/json", "Required (XML for GET_CUSTOMER_DETAILS)"], widths)
    pdf.table_row(["Authorization", "Bearer {token}", "Confirm auth type with Plintron"], widths)
    pdf.table_row(["Accept", "application/json", "Recommended"], widths)
    pdf.ln(3)

    pdf.section_title("4. Step 0 - GET_CUSTOMER_DETAILS (Optional)")
    pdf.sub_title("Purpose")
    pdf.body_text("Retrieve customer details before port-in for validation.")
    pdf.sub_title("Request Parameters")
    pdf.table_row(["Parameter", "Required", "Description"], [40, 30, 120], header=True)
    pdf.table_row(["MSISDN", "Yes", "Number to port (with country code)"], [40, 30, 120])
    pdf.table_row(["Country", "Yes", "e.g. USA"], [40, 30, 120])
    pdf.sub_title("Curl")
    pdf.code_block(
        "curl --location \"${MNP_BASE_URL}\" \\\n"
        "  --header \"Content-Type: application/xml\" \\\n"
        "  --header \"Authorization: Bearer ${AUTH_TOKEN}\" \\\n"
        "  --data-raw '<GetCustomerDetails>\n"
        "  <MSISDN>18855776204</MSISDN>\n"
        "  <Country>USA</Country>\n"
        "</GetCustomerDetails>'"
    )

    pdf.add_page()
    pdf.section_title("5. Step 1 - INITIATE_PORTIN_REQUEST")
    pdf.sub_title("Purpose")
    pdf.body_text("Submit a new port-in request. Save REFERENCE_CODE from the response.")
    pdf.sub_title("Mandatory Parameters")
    pdf.table_row(["Parameter", "Description"], [50, 140], header=True)
    pdf.table_row(["ICC_ID", "New Lyca SIM ICCID"], [50, 140])
    pdf.table_row(["P_MSISDN", "Number being ported from donor"], [50, 140])
    pdf.table_row(["ACCOUNT_NUMBER", "Account number with donor operator"], [50, 140])
    pdf.table_row(["PWD_PIN", "PIN/password for donor verification"], [50, 140])
    pdf.table_row(["CHANNEL", "e.g. MNP"], [50, 140])
    pdf.sub_title("Optional Parameters")
    pdf.body_text(
        "MSISDN, P_ICC_ID, DONOR, ZIP_CODE, SSN_TAX_ID, PORTIN_DATE (YYYY-MM-DD), "
        "PORTIN_TIME (HH:MM), ORDER_TYPE, NAME, STREET_NUMBER, STREET_NAME, "
        "STREET_DIRECTIONAL, CITY, STATE, COUNTRY, CHANGE_TYPE, PACKAGE, CONTACTNO"
    )
    pdf.sub_title("Curl - Flat JSON")
    pdf.code_block(
        "curl --location \"${MNP_BASE_URL}\" \\\n"
        "  --header \"Content-Type: application/json\" \\\n"
        "  --header \"Authorization: Bearer ${AUTH_TOKEN}\" \\\n"
        "  --data '{\n"
        "    \"SUBMIT_PORTIN_REQUEST\": {\n"
        "      \"ICC_ID\": \"8914800000123456789\",\n"
        "      \"P_MSISDN\": \"19123456780\",\n"
        "      \"ACCOUNT_NUMBER\": \"ACC998877\",\n"
        "      \"PWD_PIN\": \"1234\",\n"
        "      \"ZIP_CODE\": \"29201\",\n"
        "      \"STREET_NUMBER\": \"42\",\n"
        "      \"STREET_NAME\": \"Main Street\",\n"
        "      \"STREET_DIRECTIONAL\": \"N\",\n"
        "      \"CITY\": \"Columbia\",\n"
        "      \"STATE\": \"SC\",\n"
        "      \"COUNTRY\": \"USA\",\n"
        "      \"PORTIN_DATE\": \"2026-09-10\",\n"
        "      \"PORTIN_TIME\": \"10:00\",\n"
        "      \"CHANNEL\": \"MNP\"\n"
        "    }\n"
        "  }'"
    )
    pdf.sub_title("Success Response")
    pdf.code_block(
        "{\n"
        "  \"SUBMIT_PORTIN_RESPONSE\": {\n"
        "    \"ERROR_CODE\": \"0\",\n"
        "    \"DESCRIPTION\": \"Port-in request submitted\",\n"
        "    \"BODY\": {\n"
        "      \"REFERENCE_CODE\": \"REF20260520ABC123\",\n"
        "      \"RETURN_DESC\": \"Request processed successfully\"\n"
        "    }\n"
        "  }\n"
        "}"
    )
    pdf.sub_title("Key Error Codes")
    pdf.table_row(["Code", "Description"], [30, 160], header=True)
    pdf.table_row(["0", "Success"], [30, 160])
    pdf.table_row(["145", "Duplicate request"], [30, 160])
    pdf.table_row(["148", "Request in progress"], [30, 160])
    pdf.table_row(["156", "Invalid port-in date"], [30, 160])
    pdf.table_row(["211", "Port-in conflicts with port-out"], [30, 160])
    pdf.table_row(["310", "Port-in eligibility failure"], [30, 160])
    pdf.table_row(["99", "Other / generic failure"], [30, 160])

    pdf.add_page()
    pdf.section_title("6. Step 2 - CHECK_PORTIN_STATUS")
    pdf.sub_title("Purpose")
    pdf.body_text("Poll port-in status until terminal state: Completed, Rejected, or Cancelled.")
    pdf.sub_title("Request Parameters")
    pdf.table_row(["Parameter", "Required", "Description"], [45, 25, 120], header=True)
    pdf.table_row(["REFERENCE_NUMBER", "Yes", "REF_CODE, ICC_ID, or P_MSISDN value"], [45, 25, 120])
    pdf.table_row(["REFERENCE_TYPE", "Yes", "REF_CODE | ICC_ID | PMSISDN"], [45, 25, 120])
    pdf.sub_title("Curl - By Reference Code")
    pdf.code_block(
        "curl --location \"${MNP_BASE_URL}\" \\\n"
        "  --header \"Content-Type: application/json\" \\\n"
        "  --header \"Authorization: Bearer ${AUTH_TOKEN}\" \\\n"
        "  --data '{\n"
        "    \"GET_PORTIN_DETAILS_REQUEST\": {\n"
        "      \"DETAILS\": {\n"
        "        \"REFERENCE_NUMBER\": \"REF20260520ABC123\",\n"
        "        \"REFERENCE_TYPE\": \"REF_CODE\"\n"
        "      }\n"
        "    }\n"
        "  }'"
    )
    pdf.sub_title("Status Values")
    pdf.table_row(["STATUS", "Action"], [60, 130], header=True)
    pdf.table_row(["Initiated", "Keep polling"], [60, 130])
    pdf.table_row(["Authorized", "Keep polling"], [60, 130])
    pdf.table_row(["InProgress", "Keep polling"], [60, 130])
    pdf.table_row(["Completed", "Stop - run Step 3"], [60, 130])
    pdf.table_row(["Rejected", "Stop - handle REJECT_REASON"], [60, 130])
    pdf.table_row(["Cancelled", "Stop - port-in cancelled"], [60, 130])
    pdf.sub_title("Response Fields")
    pdf.body_text(
        "REFERENCE_CODE, MSISDN, P_MSISDN, DONOR, RECIPIENT, STATUS, REQUEST_DATE, "
        "PORTIN_DATE, REJECT_REASON, CHANNEL, "
        "COUNTRY_SPECIFIC_PARAM.USA.COMPLETED_DATE (when Completed)"
    )
    pdf.sub_title("Error Codes")
    pdf.table_row(["Code", "Description"], [30, 160], header=True)
    pdf.table_row(["0", "Success"], [30, 160])
    pdf.table_row(["105", "Reference number empty/invalid"], [30, 160])
    pdf.table_row(["106", "Reference type empty/invalid"], [30, 160])
    pdf.table_row(["107", "No records found"], [30, 160])

    pdf.add_page()
    pdf.section_title("7. Step 3 - GET_ACCOUNT_DETAILS (Post Port-In Verify)")
    pdf.sub_title("Purpose")
    pdf.body_text("After STATUS = Completed, verify MNP_IND = 1 (Ported-in) via OCS/NSS.")
    pdf.sub_title("Curl")
    pdf.code_block(
        "curl --location \"${ITG_BASE_URL}\" \\\n"
        "  --header \"Content-Type: application/json\" \\\n"
        "  --header \"Authorization: Bearer ${AUTH_TOKEN}\" \\\n"
        "  --data '{\n"
        "    \"ENVELOPE\": {\n"
        "      \"HEADER\": {\n"
        "        \"TRANSACTION_ID\": \"PORTIN-VERIFY-001\",\n"
        "        \"ENTITY\": \"ITGSIM\",\n"
        "        \"CHANNEL_REFERENCE\": \"CCARE\"\n"
        "      },\n"
        "      \"BODY\": {\n"
        "        \"GET_ACCOUNT_DETAILS_REQUEST\": {\n"
        "          \"MSISDN\": \"19123456780\"\n"
        "        }\n"
        "      }\n"
        "    }\n"
        "  }'"
    )
    pdf.sub_title("MNP_IND Values")
    pdf.table_row(["Value", "Meaning"], [40, 150], header=True)
    pdf.table_row(["0", "Regular"], [40, 150])
    pdf.table_row(["1", "Ported-in (expected after success)"], [40, 150])
    pdf.table_row(["2", "Ported-out"], [40, 150])

    pdf.section_title("8. Optional APIs")
    pdf.sub_title("INITIATE_CANCEL_PORTIN")
    pdf.body_text("Cancel port-in within donor-defined window. Requires REFERENCE_CODE, P_MSISDN, CHANNEL.")
    pdf.sub_title("MODIFY_PORTIN_REQUEST")
    pdf.body_text("Modify port-in details after submission. Requires REFERENCE_CODE plus updated port-in fields.")

    pdf.section_title("9. Postman Collection")
    pdf.body_text(
        "Import these files from docs/mnp-portin-integration/:\n"
        "  - MNP_PortIn_Postman_Collection.json\n"
        "  - MNP_PortIn_Postman_Environment.json\n\n"
        "In Postman: Import > select both files > set environment variables > run requests in order."
    )

    pdf.section_title("10. Document Sources")
    pdf.body_text(
        "MNP Proxy Specification Document V1.1 (USA) - Port-in APIs\n"
        "API Specification Document USA V1.3 - GET_ACCOUNT_DETAILS, MNP_IND\n"
        "AFG API Specification Document - No MNP content\n\n"
        "Note: Base URLs and auth credentials are NOT in the PDFs. Obtain from Plintron/Lyca integration pack."
    )

    pdf.output(str(OUT))
    print(f"PDF created: {OUT}")


if __name__ == "__main__":
    build_pdf()
