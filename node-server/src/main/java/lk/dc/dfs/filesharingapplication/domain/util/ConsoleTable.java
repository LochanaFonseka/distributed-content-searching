package lk.dc.dfs.filesharingapplication.domain.util;

import java.util.ArrayList;

public class ConsoleTable {
    private static final int CELL_PADDING = 4;
    private static final char HORIZONTAL_SEPARATOR = '-';
    private static final char VERTICAL_SEPARATOR = '|';
    private static final String PADDING_SPACES = " ".repeat(CELL_PADDING);

    private final List<String> headers;
    private final List<List<String>> rows;
    private final List<Integer> columnWidths;

    public ConsoleTable(List<String> headers, List<List<String>> data) {
        this.headers = new ArrayList<>(headers);
        this.rows = new ArrayList<>(data);
        this.columnWidths = new ArrayList<>();

        initializeColumnWidths();
        calculateAllColumnWidths();
    }

    public void updateCell(int rowIndex, int columnIndex, String value) {
        validateIndices(rowIndex, columnIndex);
        rows.get(rowIndex).set(columnIndex, value);
        calculateColumnWidth(columnIndex);
    }

    public void display() {
        String horizontalBorder = createHorizontalBorder();
        StringBuilder output = new StringBuilder();

        output.append(horizontalBorder).append("\n");
        output.append(createHeaderRow()).append("\n");
        output.append(horizontalBorder).append("\n");

        for (List<String> row : rows) {
            output.append(createDataRow(row)).append("\n");
            output.append(horizontalBorder).append("\n");
        }

        System.out.print(output);
    }

    private void initializeColumnWidths() {
        for (String header : headers) {
            columnWidths.add(header.length());
        }
    }

    private void calculateAllColumnWidths() {
        for (int col = 0; col < headers.size(); col++) {
            calculateColumnWidth(col);
        }
    }

    private void calculateColumnWidth(int columnIndex) {
        int maxWidth = headers.get(columnIndex).length();

        for (List<String> row : rows) {
            String cell = row.get(columnIndex);
            if (cell.length() > maxWidth) {
                maxWidth = cell.length();
            }
        }

        columnWidths.set(columnIndex, maxWidth);
    }

    private String createHorizontalBorder() {
        StringBuilder border = new StringBuilder();
        border.append(VERTICAL_SEPARATOR);

        for (int width : columnWidths) {
            int segmentLength = width + (2 * CELL_PADDING);
            border.append(String.valueOf(HORIZONTAL_SEPARATOR).repeat(segmentLength));
            border.append(VERTICAL_SEPARATOR);
        }

        return border.toString();
    }

    private String createHeaderRow() {
        return createRow(headers);
    }

    private String createDataRow(List<String> rowData) {
        return createRow(rowData);
    }

    private String createRow(List<String> cells) {
        StringBuilder rowBuilder = new StringBuilder();
        rowBuilder.append(VERTICAL_SEPARATOR);

        for (int i = 0; i < cells.size(); i++) {
            String cell = cells.get(i);
            rowBuilder.append(PADDING_SPACES)
                    .append(cell)
                    .append(" ".repeat(columnWidths.get(i) - cell.length()))
                    .append(PADDING_SPACES)
                    .append(VERTICAL_SEPARATOR);
        }

        return rowBuilder.toString();
    }

    private void validateIndices(int row, int col) {
        if (row < 0 || row >= rows.size()) {
            throw new IndexOutOfBoundsException("Invalid row index");
        }
        if (col < 0 || col >= headers.size()) {
            throw new IndexOutOfBoundsException("Invalid column index");
        }
    }
}
