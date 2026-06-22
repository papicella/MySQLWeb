/**
 * Lightweight client-side table controls (search, sort, pagination)
 * to replace DataTables on pages that only need basic interactivity.
 */
function initInteractiveTable(table, options) {
    if (!table || table.dataset.interactiveTableInit === 'true') {
        return
    }

    var settings = {
        pageLength: 10,
        pageLengthOptions: [10, 25, 50, 100, -1],
        searchable: true,
        sortable: true,
        paginate: true
    }

    if (options) {
        Object.keys(options).forEach(function (key) {
            settings[key] = options[key]
        })
    }

    var tbody = table.tBodies[0]
    if (!tbody) {
        return
    }

    var allRows = Array.prototype.slice.call(tbody.rows)
    var filteredRows = allRows.slice()
    var currentPage = 1
    var pageLength = settings.pageLength
    var sortColumn = -1
    var sortDirection = 'asc'
    var searchTerm = ''

    var wrapper = document.createElement('div')
    wrapper.className = 'interactive-table-wrapper'
    table.parentNode.insertBefore(wrapper, table)
    wrapper.appendChild(table)
    table.classList.add('interactive-table')

    var toolbar = document.createElement('div')
    toolbar.className = 'table-controls-toolbar'

    var footer = document.createElement('div')
    footer.className = 'table-controls-footer'
    wrapper.appendChild(footer)

    var lengthControl = document.createElement('div')
    lengthControl.className = 'table-controls-length'

    var filterControl = document.createElement('div')
    filterControl.className = 'table-controls-filter'

    var actionsGroup = document.createElement('div')
    actionsGroup.className = 'table-controls-actions'

    var infoControl = document.createElement('div')
    infoControl.className = 'table-controls-info'

    var paginateControl = document.createElement('nav')
    paginateControl.className = 'table-controls-paginate'
    paginateControl.setAttribute('aria-label', 'Table pagination')

    footer.appendChild(infoControl)
    footer.appendChild(paginateControl)

    if (settings.paginate) {
        var lengthLabel = document.createElement('label')
        lengthLabel.textContent = 'Show '
        var lengthSelect = document.createElement('select')
        lengthSelect.className = 'input'

        settings.pageLengthOptions.forEach(function (optionValue) {
            var option = document.createElement('option')
            option.value = String(optionValue)
            option.textContent = optionValue === -1 ? 'All' : String(optionValue)
            if (optionValue === settings.pageLength) {
                option.selected = true
            }
            lengthSelect.appendChild(option)
        })

        lengthLabel.appendChild(lengthSelect)
        lengthLabel.appendChild(document.createTextNode(' entries'))
        lengthControl.appendChild(lengthLabel)

        lengthSelect.addEventListener('change', function () {
            pageLength = Number(lengthSelect.value)
            currentPage = 1
            renderTable()
        })

        actionsGroup.appendChild(lengthControl)
    }

    if (settings.searchable) {
        var filterLabel = document.createElement('label')
        filterLabel.textContent = 'Search: '
        var filterInput = document.createElement('input')
        filterInput.type = 'search'
        filterInput.className = 'input'
        filterInput.setAttribute('aria-label', 'Search table')
        filterLabel.appendChild(filterInput)
        filterControl.appendChild(filterLabel)

        filterInput.addEventListener('input', function () {
            searchTerm = filterInput.value.trim().toLowerCase()
            currentPage = 1
            applyFilters()
        })

        actionsGroup.appendChild(filterControl)
    }

    if (actionsGroup.childNodes.length > 0) {
        var controlsHostSelector = table.getAttribute('data-controls-host')
        var controlsHost = controlsHostSelector
            ? document.querySelector(controlsHostSelector)
            : null

        if (controlsHost) {
            controlsHost.appendChild(actionsGroup)
        } else {
            toolbar.appendChild(actionsGroup)
            wrapper.insertBefore(toolbar, table)
        }
    }

    if (settings.sortable && table.tHead) {
        var headers = table.tHead.rows[0].cells
        Array.prototype.forEach.call(headers, function (header, columnIndex) {
            header.classList.add('sortable')
            header.addEventListener('click', function () {
                if (sortColumn === columnIndex) {
                    sortDirection = sortDirection === 'asc' ? 'desc' : 'asc'
                } else {
                    sortColumn = columnIndex
                    sortDirection = 'asc'
                }
                updateSortIndicators()
                applyFilters()
            })
        })
    }

    table.dataset.interactiveTableInit = 'true'

    applyFilters()

    /**
     * @returns {void}
     */
    function applyFilters() {
        filteredRows = allRows.filter(function (row) {
            if (!searchTerm) {
                return true
            }

            return row.textContent.toLowerCase().indexOf(searchTerm) !== -1
        })

        if (sortColumn >= 0) {
            filteredRows.sort(function (rowA, rowB) {
                return compareRows(rowA, rowB, sortColumn, sortDirection)
            })
        }

        renderTable()
    }

    /**
     * @returns {void}
     */
    function renderTable() {
        var totalRows = filteredRows.length
        var totalPages = pageLength === -1 ? 1 : Math.max(1, Math.ceil(totalRows / pageLength))
        if (currentPage > totalPages) {
            currentPage = totalPages || 1
        }

        var startIndex = pageLength === -1 ? 0 : (currentPage - 1) * pageLength
        var endIndex = pageLength === -1 ? totalRows : Math.min(startIndex + pageLength, totalRows)
        var visibleRows = filteredRows.slice(startIndex, endIndex)
        var visibleSet = new Set(visibleRows)

        var hiddenFromFilter = allRows.filter(function (row) {
            return filteredRows.indexOf(row) === -1
        })
        var orderedRows = filteredRows.concat(hiddenFromFilter)

        orderedRows.forEach(function (row) {
            tbody.appendChild(row)

            if (filteredRows.indexOf(row) === -1 || !visibleSet.has(row)) {
                row.classList.add('interactive-table-row-hidden')
                return
            }

            row.classList.remove('interactive-table-row-hidden')
        })

        updateInfo(startIndex, endIndex, totalRows)
        updatePagination(totalPages)
    }

    /**
     * @param {number} startIndex
     * @param {number} endIndex
     * @param {number} totalRows
     * @returns {void}
     */
    function updateInfo(startIndex, endIndex, totalRows) {
        if (!settings.paginate) {
            infoControl.textContent = ''
            return
        }

        if (totalRows === 0) {
            var filteredMessage = searchTerm
                ? ' (filtered from ' + allRows.length + ' total entries)'
                : ''
            infoControl.textContent = 'Showing 0 to 0 of 0 entries' + filteredMessage
            return
        }

        var from = startIndex + 1
        var to = endIndex
        var message = 'Showing ' + from + ' to ' + to + ' of ' + totalRows + ' entries'

        if (searchTerm && allRows.length !== totalRows) {
            message += ' (filtered from ' + allRows.length + ' total entries)'
        }

        infoControl.textContent = message
    }

    /**
     * @param {number} totalPages
     * @returns {void}
     */
    function updatePagination(totalPages) {
        paginateControl.innerHTML = ''

        if (!settings.paginate || pageLength === -1 || totalPages <= 1) {
            return
        }

        var list = document.createElement('ul')
        list.className = 'pagination'

        list.appendChild(createPageItem('Previous', currentPage === 1, function () {
            if (currentPage > 1) {
                currentPage -= 1
                renderTable()
            }
        }))

        for (var page = 1; page <= totalPages; page += 1) {
            list.appendChild(createPageItem(String(page), false, function (pageNumber) {
                return function () {
                    currentPage = pageNumber
                    renderTable()
                }
            }(page), page === currentPage))
        }

        list.appendChild(createPageItem('Next', currentPage === totalPages, function () {
            if (currentPage < totalPages) {
                currentPage += 1
                renderTable()
            }
        }))

        paginateControl.appendChild(list)
    }

    /**
     * @param {string} label
     * @param {boolean} isDisabled
     * @param {Function} onClick
     * @param {boolean} isActive
     * @returns {HTMLLIElement}
     */
    function createPageItem(label, isDisabled, onClick, isActive) {
        var item = document.createElement('li')
        if (isDisabled) {
            item.className = 'disabled'
        }
        if (isActive) {
            item.className = 'active'
        }

        var link = document.createElement('a')
        link.href = '#'
        link.textContent = label
        link.addEventListener('click', function (event) {
            event.preventDefault()
            if (!isDisabled) {
                onClick()
            }
        })

        item.appendChild(link)
        return item
    }

    /**
     * @returns {void}
     */
    function updateSortIndicators() {
        if (!table.tHead) {
            return
        }

        var headers = table.tHead.rows[0].cells
        Array.prototype.forEach.call(headers, function (header, columnIndex) {
            header.classList.remove('sort-asc', 'sort-desc')
            if (columnIndex === sortColumn) {
                header.classList.add(sortDirection === 'asc' ? 'sort-asc' : 'sort-desc')
            }
        })
    }
}

/**
 * @param {HTMLTableRowElement} rowA
 * @param {HTMLTableRowElement} rowB
 * @param {number} columnIndex
 * @param {string} direction
 * @returns {number}
 */
function compareRows(rowA, rowB, columnIndex, direction) {
    var valueA = getCellSortValue(rowA.cells[columnIndex])
    var valueB = getCellSortValue(rowB.cells[columnIndex])
    var numericA = Number(valueA)
    var numericB = Number(valueB)
    var result = 0

    if (valueA !== '' && valueB !== '' && !isNaN(numericA) && !isNaN(numericB)) {
        result = numericA - numericB
    } else {
        result = valueA.localeCompare(valueB, undefined, { sensitivity: 'base' })
    }

    return direction === 'asc' ? result : -result
}

/**
 * @param {HTMLTableCellElement|undefined} cell
 * @returns {string}
 */
function getCellSortValue(cell) {
    if (!cell) {
        return ''
    }

    var checkbox = cell.querySelector('input[type="checkbox"]')
    if (checkbox) {
        return checkbox.checked ? '1' : '0'
    }

    return cell.textContent.trim()
}

document.addEventListener('DOMContentLoaded', function () {
    var options = {
        pageLength: 10,
        pageLengthOptions: [10, 25, 50, 100, -1]
    }
    var initialized = new Set()

    document.querySelectorAll('[data-interactive-table]').forEach(function (table) {
        initInteractiveTable(table, options)
        initialized.add(table)
    })

    var legacyTable = document.getElementById('datatable-mysql')
    if (legacyTable && !initialized.has(legacyTable)) {
        initInteractiveTable(legacyTable, options)
    }
})
