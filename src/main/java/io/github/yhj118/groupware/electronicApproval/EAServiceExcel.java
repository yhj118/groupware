package io.github.yhj118.groupware.electronicApproval;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 6개 상세보기 HTML의 배치와 표시 항목을 셀로 재현합니다. */
@Service
public class EAServiceExcel {

    // 기존 컨트롤러 호출 유지. 별도 테이블의 물품/첨부는 아래 4개 파라미터 메서드로 넘깁니다.
    public byte[] createProposalExcel(EAResponse document) throws IOException {
        return createProposalExcel(document, document, List.of(), List.of());
    }

    public byte[] createApprovalExcel(EAResponse document, EAResponse writerInfo,
                                      List<EARequestItem> items,
                                      List<EAResponseAttachment> attachments) throws IOException {
        return createProposalExcel(document, writerInfo, items, attachments);
    }

    public byte[] createProposalExcel(EAResponse document, EAResponse writerInfo,
                                      List<EARequestItem> items,
                                      List<EAResponseAttachment> attachments) throws IOException {
        if (document == null || document.getEaType() == null) {
            throw new IllegalArgumentException("문서 정보가 없습니다.");
        }
        items = items == null ? List.of() : items;
        attachments = attachments == null ? List.of() : attachments;
        String eaType = document.getEaType();

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if ("PROPOSAL".equals(eaType)) {
                // detailProposal: 왼쪽 제목 → 4열 작성자 정보 → 날짜 띠 → 목적/내용 → 파일 카드.
                DetailSheet form = new DetailSheet(workbook, "기안서");
                form.documentHeader(document, "기안서", document.getProposalTitle());
                form.metadata(document, writerInfo, "기안 부서", "기안 팀", "기안자");
                form.dates(document);
                form.textSection("기안 목적", document.getProposalPurpose(), "등록된 기안 목적이 없습니다.");
                form.textSection("상세 내용", document.getProposalContent(), "등록된 상세 내용이 없습니다.");
                form.fileCards(attachments, true);
                form.comment(document.getApprovalComment(), true);
                form.finish(document.getApprovalId(), "기안서");

            } else if ("APPROVAL_REQUEST".equals(eaType)) {
                // detailApprovalRequest: 내부 헤더 → 4열 × 2행 정보 → 제목 → 사유 → 물품 표/총액.
                DetailSheet form = new DetailSheet(workbook, "품의서");
                form.requestHeader(document);
                form.requestMetadata(document, writerInfo);
                form.requestTitle(document.getApprovalRequestTitle());
                form.textSection("품의 사유", document.getApprovalRequestContent(), "—");
                form.purchaseItems(items, document.getApprovalRequestTotalMoney());
                form.fileCards(attachments, false);
                form.comment(document.getApprovalComment(), false);
                form.finish(document.getApprovalId(), null);

            } else if ("VACATION".equals(eaType)) {
                // detailVacation: 휴가 종류/시작/종료를 큰 값이 들어간 3개 카드로 표시합니다.
                DetailSheet form = new DetailSheet(workbook, "휴가 신청서");
                form.documentHeader(document, "휴가 신청서", vacationTitle(document.getVacationType()));
                form.metadata(document, writerInfo, "소속 부서", "소속 팀", "신청자");
                form.dates(document);
                form.sectionHeading("휴가 정보");
                form.vacationCards(vacationName(document.getVacationType()),
                        document.getVacationStart(), document.getVacationEnd());
                form.period("신청일", document.getVacationSubscription());
                form.endSection();
                form.textSection("업무 인수인계", document.getVacationContent(), "—");
                form.comment(document.getApprovalComment(), true);
                form.finish(document.getApprovalId(), "휴가 신청서");

            } else if ("BUSINESS_TRIP".equals(eaType)) {
                // detailBusinessTrip: 2열 × 3행 출장 정보, 목적/일정, 별도의 경비 카드.
                DetailSheet form = new DetailSheet(workbook, "출장 계획서");
                form.documentHeader(document, "출장 계획서", document.getBusinessTripTitle());
                form.metadata(document, writerInfo, "소속 부서", "소속 팀", "출장자");
                form.dates(document);
                form.sectionHeading("출장 정보");
                form.fields(new String[]{"출장지", "동행자", "출장 시작일", "출장 종료일", "신청일", "교통수단"},
                        new Object[]{document.getBusinessTripLocation(), document.getBusinessTripFriend(),
                                document.getBusinessTripStart(), document.getBusinessTripEnd(),
                                document.getBusinessTripExecutionDate(), document.getBusinessTripTransportation()}, 2);
                form.endSection();
                form.textSection("출장 목적", document.getBusinessTripPurpose(), "—");
                form.textSection("출장 일정", document.getBusinessTripContent(), "—");
                form.sectionHeading("예상 경비");
                form.moneyCard("총 예상 경비", document.getBusinessTripAmount(), false);
                form.endSection();
                form.comment(document.getApprovalComment(), true);
                form.finish(document.getApprovalId(), "출장 계획서");

            } else if ("FACT_REPORT".equals(eaType)) {
                // detailFactReport: 발생 일시/장소의 2열 정보 → 경위 → 원인 → 대책.
                DetailSheet form = new DetailSheet(workbook, "경위 시말서");
                form.documentHeader(document, "경위/시말서", document.getFactReportTitle());
                form.metadata(document, writerInfo, "소속 부서", "소속 팀", "작성자");
                form.dates(document);
                form.sectionHeading("발생 정보");
                form.fields(new String[]{"발생 일시", "발생 장소"},
                        new Object[]{document.getFactReportOccurrence(), document.getFactReportLocation()}, 2);
                form.endSection();
                form.textSection("발생 경위", document.getFactReportInspector(), "—");
                form.textSection("원인 및 책임", document.getFactReportCause(), "—");
                form.textSection("조치 및 재발 방지 대책", document.getFactReportCountermeasures(), "—");
                form.fileCards(attachments, true);
                form.comment(document.getApprovalComment(), true);
                form.finish(document.getApprovalId(), "경위/시말서");

            } else if ("WORK_REPORT".equals(eaType)) {
                // detailWorkReport: 업무 시작/종료 2열, 다음 행 보고일, 네 가지 보고 항목.
                DetailSheet form = new DetailSheet(workbook, "업무 보고서");
                form.documentHeader(document, "업무 보고서", document.getWorkReportTitle());
                form.metadata(document, writerInfo, "소속 부서", "소속 팀", "보고자");
                form.dates(document);
                form.sectionHeading("업무 기간");
                form.fields(new String[]{"업무 시작일", "업무 종료일", "보고일"},
                        new Object[]{document.getWorkReportStart(), document.getWorkReportEnd(), document.getWorkReportDate()}, 2);
                form.endSection();
                form.textSection("업무 개요", document.getWorkReportOutline(), "—");
                form.textSection("진행 내용", document.getWorkReportContent(), "—");
                form.textSection("결과 및 성과", document.getWorkReportResult(), "—");
                form.textSection("향후 계획 및 특이사항", document.getWorkReportEtc(), "—");
                form.fileCards(attachments, true);
                form.comment(document.getApprovalComment(), true);
                form.finish(document.getApprovalId(), "업무 보고서");

            } else {
                throw new IllegalArgumentException("지원하지 않는 문서 종류: " + eaType);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // 앞서 만든 다운로드 컨트롤러의 파일 이름 생성 호출도 유지합니다.
    public static String formName(String type) {
        if ("PROPOSAL".equals(type)) return "기안서";
        if ("APPROVAL_REQUEST".equals(type)) return "품의서";
        if ("VACATION".equals(type)) return "휴가 신청서";
        if ("BUSINESS_TRIP".equals(type)) return "출장 계획서";
        if ("FACT_REPORT".equals(type)) return "경위 시말서";
        if ("WORK_REPORT".equals(type)) return "업무 보고서";
        throw new IllegalArgumentException("지원하지 않는 문서 종류: " + type);
    }

    private static String vacationName(String type) {
        if (type == null) return "—";
        return switch (type) {
            case "ANNUAL" -> "연차";
            case "HALF_AM" -> "오전 반차";
            case "HALF_PM", "HALF_FM" -> "오후 반차";
            case "SICK" -> "병가";
            case "ETC" -> "기타";
            default -> type;
        };
    }

    private static String vacationTitle(String type) {
        if ("ANNUAL".equals(type)) return "연차 휴가 신청";
        if ("ETC".equals(type)) return "기타";
        return vacationName(type) + " 신청";
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    // 한글 폭 2, 영문 폭 1. 여러 행에 기록해 긴 본문도 한 셀의 글자 제한에 걸리지 않습니다.
    static List<String> wrapLines(String value, int width) {
        List<String> lines = new ArrayList<>();
        String normalized = text(value, "—").replace("\r\n", "\n").replace('\r', '\n').replace("\t", "    ");
        for (String paragraph : normalized.split("\n", -1)) {
            StringBuilder line = new StringBuilder();
            int units = 0;
            for (int point : paragraph.codePoints().toArray()) {
                int size = point <= 255 ? 1 : 2;
                if (units + size > width && !line.isEmpty()) {
                    lines.add(line.toString());
                    line.setLength(0);
                    units = 0;
                }
                line.appendCodePoint(point);
                units += size;
            }
            lines.add(line.toString());
        }
        return lines;
    }

    /** 셀 조작/서식만 공유합니다. 각 양식의 실제 배치는 위의 else if 블록에서 정합니다. */
    private static class DetailSheet {
        private final XSSFWorkbook workbook;
        private final XSSFSheet sheet;
        private final Map<String, XSSFCellStyle> borderCache = new HashMap<>();
        private final XSSFCellStyle body, muted, label, value, formName, title, sectionTitle,
                date, timestamp, shadeLabel, shadeTimestamp, cardLabel, cardValue, cardDate,
                tableHead, center, money, totalLabel, totalMoney, tripMoney,
                fileName, fileSize, fileIcon, comment, footnoteStyle, approved, pending, rejected;
        private int nextRow;
        private int paperStart;

        DetailSheet(XSSFWorkbook workbook, String name) {
            this.workbook = workbook;
            sheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(name));
            sheet.setDisplayGridlines(false);
            sheet.setPrintGridlines(false);
            sheet.setDefaultRowHeightInPoints(20);
            // B:AA는 문서 외곽, C:Z는 내용. 좁은 열 여러 개로 카드 사이 간격을 만듭니다.
            sheet.setColumnWidth(0, 2 * 256);
            for (int c = 1; c <= 26; c++) sheet.setColumnWidth(c, (int) (3.7 * 256));
            sheet.setColumnWidth(27, 2 * 256);

            body = style(11, false, "182433", "FFFFFF", HorizontalAlignment.LEFT, null);
            muted = style(9, false, "667382", "FFFFFF", HorizontalAlignment.LEFT, null);
            label = style(9, false, "667382", "FFFFFF", HorizontalAlignment.LEFT, null);
            value = style(11, true, "182433", "FFFFFF", HorizontalAlignment.LEFT, null);
            formName = style(10, true, "4263B3", "FFFFFF", HorizontalAlignment.LEFT, null);
            title = style(19, true, "182433", "FFFFFF", HorizontalAlignment.LEFT, null);
            sectionTitle = style(12, true, "182433", "FFFFFF", HorizontalAlignment.LEFT, null);
            date = style(10, true, "182433", "FFFFFF", HorizontalAlignment.LEFT, "yyyy-mm-dd");
            timestamp = style(9, false, "182433", "FFFFFF", HorizontalAlignment.LEFT, "yyyy-mm-dd hh:mm:ss");
            shadeLabel = style(9, false, "667382", "F8FAFC", HorizontalAlignment.LEFT, null);
            shadeTimestamp = style(9, false, "182433", "F8FAFC", HorizontalAlignment.LEFT, "yyyy-mm-dd hh:mm:ss");
            cardLabel = style(9, false, "667382", "F8FAFC", HorizontalAlignment.LEFT, null);
            cardLabel.setIndention((short) 1);
            cardValue = style(14, true, "182433", "F8FAFC", HorizontalAlignment.LEFT, null);
            cardValue.setIndention((short) 1);
            cardDate = style(13, true, "182433", "F8FAFC", HorizontalAlignment.LEFT, "yyyy/mm/dd");
            cardDate.setIndention((short) 1);
            tableHead = style(10, true, "667382", "F6F8FB", HorizontalAlignment.LEFT, null);
            center = style(10, false, "667382", "FFFFFF", HorizontalAlignment.CENTER, "0");
            money = style(11, false, "182433", "FFFFFF", HorizontalAlignment.RIGHT, "#,##0\" 원\"");
            totalLabel = style(11, false, "182433", "F6F8FB", HorizontalAlignment.LEFT, null);
            totalLabel.setIndention((short) 1);
            totalMoney = style(16, true, "206BC4", "F6F8FB", HorizontalAlignment.RIGHT, "#,##0\" 원\"");
            totalMoney.setIndention((short) 1);
            tripMoney = style(17, true, "4263B3", "F8FAFC", HorizontalAlignment.RIGHT, "#,##0\" 원\"");
            tripMoney.setIndention((short) 1);
            fileName = style(10, true, "182433", "FFFFFF", HorizontalAlignment.LEFT, null);
            fileSize = style(9, false, "667382", "FFFFFF", HorizontalAlignment.LEFT, null);
            fileIcon = style(8, true, "4263B3", "EDF3FF", HorizontalAlignment.CENTER, null);
            comment = style(11, false, "182433", "F8FAFC", HorizontalAlignment.LEFT, null);
            comment.setIndention((short) 1);
            footnoteStyle = style(9, false, "667382", "FFFFFF", HorizontalAlignment.RIGHT, null);
            approved = style(10, true, "23834B", "E9F7EE", HorizontalAlignment.CENTER, null);
            pending = style(10, true, "916200", "FFF4D7", HorizontalAlignment.CENTER, null);
            rejected = style(10, true, "C43C39", "FDECEB", HorizontalAlignment.CENTER, null);
        }

        private XSSFCellStyle style(int size, boolean bold, String color, String fill,
                                    HorizontalAlignment align, String numberFormat) {
            XSSFFont font = workbook.createFont();
            font.setFontName("맑은 고딕");
            font.setFontHeightInPoints((short) size);
            font.setBold(bold);
            font.setColor(color(color));
            XSSFCellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(align);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setWrapText(true);
            style.setFillForegroundColor(color(fill));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            if (numberFormat != null) style.setDataFormat(workbook.createDataFormat().getFormat(numberFormat));
            return style;
        }

        private XSSFColor color(String hex) {
            byte[] rgb = new byte[3];
            for (int i = 0; i < 3; i++) rgb[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            return new XSSFColor(rgb, new DefaultIndexedColorMap());
        }

        private Row row(int index, float height) {
            Row row = sheet.getRow(index);
            if (row == null) row = sheet.createRow(index);
            row.setHeightInPoints(height);
            return row;
        }

        private Cell cellAt(int r, int c) {
            Row row = sheet.getRow(r);
            if (row == null) row = sheet.createRow(r);
            Cell cell = row.getCell(c);
            if (cell == null) cell = row.createCell(c);
            return cell;
        }

        private void fill(int r1, int r2, int c1, int c2, XSSFCellStyle style) {
            for (int r = r1; r <= r2; r++) for (int c = c1; c <= c2; c++) cellAt(r, c).setCellStyle(style);
        }

        private void merge(int r1, int r2, int c1, int c2, Object v, XSSFCellStyle style) {
            fill(r1, r2, c1, c2, style);
            Cell cell = cellAt(r1, c1);
            if (v == null || v instanceof String && ((String) v).isBlank()) cell.setBlank();
            else if (v instanceof LocalDateTime t) cell.setCellValue(t);
            else if (v instanceof LocalDate d) cell.setCellValue(d);
            else if (v instanceof Number n) cell.setCellValue(n.doubleValue());
            else cell.setCellValue(v.toString());
            if (r1 != r2 || c1 != c2) sheet.addMergedRegion(new CellRangeAddress(r1, r2, c1, c2));
        }

        private void line(int r, int c1, int c2, Object v, XSSFCellStyle style) {
            merge(r, r, c1, c2, v, style);
        }

        private void edge(int r, int c, boolean top, boolean bottom, boolean left, boolean right,
                          BorderStyle weight, String hex) {
            Cell cell = cellAt(r, c);
            XSSFCellStyle base = (XSSFCellStyle) cell.getCellStyle();
            String key = base.getIndex() + ":" + top + bottom + left + right + ":" + weight + ":" + hex;
            XSSFCellStyle result = borderCache.get(key);
            if (result == null) {
                result = workbook.createCellStyle();
                result.cloneStyleFrom(base);
                XSSFColor color = color(hex);
                if (top) { result.setBorderTop(weight); result.setTopBorderColor(color); }
                if (bottom) { result.setBorderBottom(weight); result.setBottomBorderColor(color); }
                if (left) { result.setBorderLeft(weight); result.setLeftBorderColor(color); }
                if (right) { result.setBorderRight(weight); result.setRightBorderColor(color); }
                borderCache.put(key, result);
            }
            cell.setCellStyle(result);
        }

        private void box(int r1, int r2, int c1, int c2, BorderStyle weight, String hex) {
            for (int r = r1; r <= r2; r++) for (int c = c1; c <= c2; c++) {
                if (r == r1 || r == r2 || c == c1 || c == c2) edge(r, c, r == r1, r == r2, c == c1, c == c2, weight, hex);
            }
        }

        private void bottom(int r, int c1, int c2) {
            for (int c = c1; c <= c2; c++) edge(r, c, false, true, false, false, BorderStyle.THIN, "E2E8F0");
        }

        private void space(float height) { row(nextRow++, height); }

        private void status(EAResponse d, int r1, int r2) {
            String s = d.getApprovalStatus();
            String name = "APPROVE".equals(s) ? "승인 완료" : "PENDING".equals(s) ? "결재 대기" : "REJECT".equals(s) ? "반려" : "상태 확인 필요";
            merge(r1, r2, 21, 25, name, "APPROVE".equals(s) ? approved : "REJECT".equals(s) ? rejected : pending);
        }

        void documentHeader(EAResponse d, String name, String heading) {
            row(0, 8); row(1, 18); row(2, 18); row(3, 12);
            line(1, 2, 20, "ELECTRONIC APPROVAL", muted);
            line(2, 2, 20, "문서 번호 · " + d.getApprovalId(), muted);
            status(d, 1, 2);
            paperStart = 4;
            nextRow = paperStart;
            space(15);
            row(nextRow, 21); line(nextRow++, 2, 25, name, formName);
            List<String> titleLines = wrapLines(text(heading, "제목 없음"), 57);
            for (int i = 0; i < titleLines.size(); i++) { row(nextRow, 29); line(nextRow++, 2, 25, titleLines.get(i), title); }
            space(16); bottom(nextRow - 1, 1, 26);
        }

        void requestHeader(EAResponse d) {
            row(0, 8); paperStart = 1; nextRow = 1;
            space(14);
            row(nextRow, 18); line(nextRow, 2, 20, "ELECTRONIC APPROVAL", muted);
            int statusStart = nextRow++;
            row(nextRow, 29); line(nextRow, 2, 20, "품의서 상세보기", title);
            status(d, statusStart, nextRow++);
            space(14); bottom(nextRow - 1, 1, 26);
            space(14);
        }

        void metadata(EAResponse d, EAResponse writerInfo, String departmentLabel, String teamLabel, String writerLabel) {
            space(12);
            fields(new String[]{departmentLabel, teamLabel, writerLabel, "결재자"},
                    new Object[]{writerInfo == null ? null : writerInfo.getDepartmentType(),
                            writerInfo == null ? null : writerInfo.getDepartmentName(), d.getEmployeeName(), d.getApproverName()}, 4);
            space(10); bottom(nextRow - 1, 1, 26);
        }

        void dates(EAResponse d) {
            int start = nextRow;
            space(10);
            fields(new String[]{"작성일시", "수정일시", "처리일시"},
                    new Object[]{d.getApprovalWriteAt(), d.getApprovalUpdateAt(), d.getApprovalProcessedAt()}, 3, true);
            space(10);
            // 날짜 띠의 빈 열과 양옆 여백에도 같은 회색 배경을 채웁니다.
            for (int r = start; r < nextRow; r++) for (int c = 1; c <= 26; c++) {
                if (cellAt(r, c).getCellStyle().getIndex() == 0) cellAt(r, c).setCellStyle(shadeLabel);
            }
            bottom(nextRow - 1, 1, 26);
        }

        void requestMetadata(EAResponse d, EAResponse writerInfo) {
            fields(new String[]{"소속 부서", "소속 팀", "기안자", "결재권자", "작성일시", "수정일시", "처리일시", "집행 예정일"},
                    new Object[]{writerInfo == null ? null : writerInfo.getDepartmentType(),
                            writerInfo == null ? null : writerInfo.getDepartmentName(), d.getEmployeeName(), d.getApproverName(),
                            d.getApprovalWriteAt(), d.getApprovalUpdateAt(), d.getApprovalProcessedAt(), d.getApprovalRequestExecutionDate()}, 4);
            space(5);
        }

        void fields(String[] labels, Object[] values, int columns) { fields(labels, values, columns, false); }

        private void fields(String[] labels, Object[] values, int columns, boolean shaded) {
            int span = 24 / columns;
            for (int g = 0; g < labels.length; g += columns) {
                int start = nextRow;
                row(start, 18);
                int count = 1;
                for (int i = g; i < Math.min(g + columns, labels.length); i++) {
                    if (values[i] instanceof String s) count = Math.max(count, wrapLines(s, columns == 2 ? 42 : columns == 3 ? 27 : 19).size());
                }
                for (int j = 0; j < count; j++) row(start + 1 + j, 22);
                for (int i = g; i < Math.min(g + columns, labels.length); i++) {
                    int first = 2 + (i - g) * span, last = first + span - 2;
                    line(start, first, last, labels[i], shaded ? shadeLabel : label);
                    Object v = values[i];
                    List<String> lines = v instanceof String s ? wrapLines(s, columns == 2 ? 42 : columns == 3 ? 27 : 19) : List.of("");
                    XSSFCellStyle format = shaded ? shadeTimestamp : v instanceof LocalDateTime ? timestamp : v instanceof LocalDate ? date : value;
                    for (int j = 0; j < count; j++) line(start + 1 + j, first, last,
                            v instanceof String ? (j < lines.size() ? lines.get(j) : "") : j == 0 ? (v == null ? "—" : v) : "", format);
                }
                nextRow = start + 1 + count;
                if (g + columns < labels.length) space(10);
            }
        }

        void sectionHeading(String name) { space(15); row(nextRow, 26); line(nextRow++, 2, 25, name, sectionTitle); space(5); }

        void endSection() { space(15); bottom(nextRow - 1, 1, 26); }

        void textSection(String name, String content, String fallback) {
            sectionHeading(name);
            for (String s : wrapLines(text(content, fallback), 92)) { row(nextRow, 22); line(nextRow++, 2, 25, s, body); }
            endSection();
        }

        void requestTitle(String heading) {
            sectionHeading("제목");
            for (String s : wrapLines(text(heading, "—"), 57)) { row(nextRow, 27); line(nextRow++, 2, 25, s, title); }
            endSection();
        }

        void vacationCards(String type, LocalDate start, LocalDate end) {
            int r = nextRow;
            row(r, 7); row(r + 1, 20); row(r + 2, 31); row(r + 3, 7);
            String[] labels = {"휴가 종류", "휴가 시작일", "휴가 종료일"}; Object[] values = {type, start, end};
            for (int i = 0; i < 3; i++) {
                int first = 2 + i * 8, last = first + 6;
                fill(r, r + 3, first, last, cardLabel);
                line(r + 1, first, last, labels[i], cardLabel);
                line(r + 2, first, last, values[i] == null ? "—" : values[i], i == 0 ? cardValue : cardDate);
                box(r, r + 3, first, last, BorderStyle.THIN, "E2E8F0");
            }
            nextRow += 4;
        }

        void period(String name, LocalDate day) {
            space(8); row(nextRow, 21);
            line(nextRow++, 2, 25, name + " · " + (day == null ? "—" : day.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))), muted);
        }

        void purchaseItems(List<EARequestItem> items, int storedTotal) {
            sectionHeading("구매 물품");
            int start = nextRow;
            row(nextRow, 27);
            line(nextRow, 2, 3, "번호", tableHead);
            line(nextRow, 4, 18, "물품 이름", tableHead);
            line(nextRow, 19, 25, "금액", tableHead);
            bottom(nextRow++, 2, 25);
            if (items.isEmpty()) { row(nextRow, 30); line(nextRow++, 2, 25, "등록된 구매 물품이 없습니다.", muted); }
            for (int i = 0; i < items.size(); i++) {
                EARequestItem item = items.get(i);
                int itemStart = nextRow;
                List<String> lines = wrapLines(item.getItemName(), 55);
                for (String s : lines) { row(nextRow, 25); line(nextRow++, 4, 18, s, body); }
                merge(itemStart, nextRow - 1, 2, 3, i + 1, center);
                merge(itemStart, nextRow - 1, 19, 25, item.getItemAmount(), money);
                bottom(nextRow - 1, 2, 25);
            }
            box(start, nextRow - 1, 2, 25, BorderStyle.THIN, "E2E8F0");
            space(10);
            // DB에 저장된 총액을 그대로 표시합니다. Excel에서 다시 계산하지 않습니다.
            moneyCard("총 품의 금액", storedTotal, true);
            endSection();
        }

        void moneyCard(String name, int amount, boolean request) {
            row(nextRow, 38);
            line(nextRow, 2, 16, name, request ? totalLabel : cardLabel);
            line(nextRow, 17, 25, amount, request ? totalMoney : tripMoney);
            if (!request) box(nextRow, nextRow, 2, 25, BorderStyle.THIN, "E2E8F0");
            nextRow++;
        }

        void fileCards(List<EAResponseAttachment> files, boolean icons) {
            sectionHeading("첨부파일 (" + files.size() + ")");
            if (files.isEmpty()) { row(nextRow, 22); line(nextRow++, 2, 25, "등록된 첨부파일이 없습니다.", muted); }
            for (int g = 0; g < files.size(); g += 2) {
                int start = nextRow, maxLines = 1;
                for (int i = g; i < Math.min(files.size(), g + 2); i++) maxLines = Math.max(maxLines, wrapLines(files.get(i).getOriginalFileName(), icons ? 32 : 42).size());
                row(start, 6);
                for (int j = 0; j < maxLines; j++) row(start + 1 + j, 21);
                row(start + 1 + maxLines, 18); row(start + 2 + maxLines, 6);
                for (int i = g; i < Math.min(files.size(), g + 2); i++) {
                    EAResponseAttachment file = files.get(i);
                    int first = 2 + (i - g) * 12, last = first + 10, nameFirst = icons ? first + 2 : first;
                    List<String> lines = wrapLines(file.getOriginalFileName(), icons ? 32 : 42);
                    if (icons) {
                        String original = text(file.getOriginalFileName(), "FILE");
                        int dot = original.lastIndexOf('.');
                        String ext = dot >= 0 ? original.substring(dot + 1).toUpperCase(java.util.Locale.ROOT) : "FILE";
                        merge(start + 1, start + 1 + maxLines, first, first + 1, ext.length() <= 5 ? ext : "FILE", fileIcon);
                    }
                    for (int j = 0; j < maxLines; j++) line(start + 1 + j, nameFirst, last, j < lines.size() ? lines.get(j) : "", fileName);
                    boolean mb = file.getFileSize() >= 1048576;
                    String size = String.format(java.util.Locale.ROOT, "%.1f %s", file.getFileSize() / (mb ? 1048576.0 : 1024.0), mb ? "MB" : "KB");
                    line(start + 1 + maxLines, nameFirst, last, size, fileSize);
                    box(start, start + 2 + maxLines, first, last, BorderStyle.THIN, "E2E8F0");
                }
                nextRow = start + 3 + maxLines;
                if (g + 2 < files.size()) space(8);
            }
            endSection();
        }

        void comment(String content, boolean accent) {
            sectionHeading("결재자 코멘트");
            int start = nextRow;
            space(8);
            for (String s : wrapLines(text(content, "등록된 코멘트가 없습니다."), 86)) { row(nextRow, 22); line(nextRow++, 2, 25, s, comment); }
            space(8);
            for (int r = start; r < nextRow; r++) {
                for (int c = 2; c <= 25; c++) if (cellAt(r, c).getCellStyle().getIndex() == 0) cellAt(r, c).setCellStyle(comment);
                if (accent) edge(r, 2, false, false, true, false, BorderStyle.MEDIUM, "A5B8DF");
            }
            space(15);
        }

        void finish(int approvalId, String footnote) {
            // HTML은 얇은 외곽선과 여백을 사용하므로, 제목/본문에 굵은 격자를 추가하지 않습니다.
            box(paperStart, nextRow - 1, 1, 26, BorderStyle.THIN, "E2E8F0");
            if (footnote != null) { space(8); row(nextRow, 18); line(nextRow++, 2, 25, "전자결재 문서 · " + footnote, footnoteStyle); }
            workbook.setPrintArea(workbook.getSheetIndex(sheet), 1, 26, 0, nextRow - 1);
            sheet.setFitToPage(true);
            PrintSetup print = sheet.getPrintSetup();
            print.setPaperSize(PrintSetup.A4_PAPERSIZE);
            print.setLandscape(false); print.setFitWidth((short) 1); print.setFitHeight((short) 0);
            sheet.setHorizontallyCenter(true);
            sheet.setMargin(Sheet.LeftMargin, 0.3); sheet.setMargin(Sheet.RightMargin, 0.3);
            sheet.setMargin(Sheet.TopMargin, 0.3); sheet.setMargin(Sheet.BottomMargin, 0.3);
            sheet.getFooter().setLeft("문서 번호 · " + approvalId);
            sheet.getFooter().setRight("&P / &N");
        }
    }
}
