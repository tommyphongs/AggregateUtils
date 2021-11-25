/*
 * @created 26/09/2020
 * @project user-data-aggregation
 * @author phongnh@coccoc.com
 */

/**
 * +------------------------------------------------------------+
 * |                                                            |
 * |                    wwwwwwww     wwwww        wwwwwwwwww    |
 * |              wwwwwwwwwwwwww     wwwww      wwwwwwwwww      |
 * |          wwwwwwwwwwwwwwwwww     wwww     wwwwwwwww         |
 * |        wwwwwwwwwwwwwwwwwwwww   wwww     wwwwww             |
 * |      wwwwwwwwwwwwwwwwwwwwww     ww    wwwww       www      |
 * |    wwwwwwwwwwwwwwwwwww               www      wwwwwwwww    |
 * |   wwwwwwwwwwwwwwwww     wwwwwwwwwww       wwwwwwww         |
 * |   wwwwwwwwwwwwwww    wwwwwwwwwwwwwwww                      |
 * |  wwwwwwwwwwwwwww     wwwwwwwwwwwwwwwww     wwwwwwwwwwwwww  |
 * |  wwwwwwwwwwwwwwww    wwwwwwwwwwwwwwwww    wwwwwwwwwwwwwww  |
 * |   wwwwwwwwwwwwwww     wwwwwwwwwwwwwww    wwwwwwwwwwwwwwww  |
 * |    wwwwwwwwwwwwwwwww       wwwww       wwwwwwwwwwwwwwwww   |
 * |     wwwwwwwwwwwwwwwwwwww           wwwwwwwwwwwwwwwwwwww    |
 * |      wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww      |
 * |         wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww        |
 * |            wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww           |
 * |                wwwwwwwwwwwwwwwwwwwwwwwwwwwww               |
 * |                        wwwwwwwwwwwww                       |
 * |                                                            |
 * +------------------------------------------------------------+
 */

package org.coding.aggregates.utils;

public interface ThrowableFunction<T, U> {

    U apply(T t) throws Exception;

}
