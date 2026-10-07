import type { ComponentType, LazyExoticComponent } from 'react';

import type { BuiltInAction } from '@/model/access';

export interface PortletProps {
  projectId: string;
}

export type PortletComponent = ComponentType<PortletProps>;

export interface PortletDescriptor {
  /** Must equal the legacy `@Portlet(id)` so saved perspective layouts keep loading (docs/03 §3). */
  id: string;
  titleKey: string;
  tooltipKey?: string;
  component: LazyExoticComponent<PortletComponent>;
  /** Missing permission renders the Forbidden placeholder. */
  requires?: BuiltInAction[];
  /** Without a selection the NothingSelected placeholder is rendered. */
  acceptsSelection?: boolean;
}

/**
 * The single place where portlets are registered (Cursor rule 8); ids follow the
 * list in docs/03 §3. Entries are added from P2-05 onwards.
 */
export const portletRegistry: readonly PortletDescriptor[] = [];

/** OBO portlets are dropped (decision D4); layouts containing them show a "disabled view". */
const DISABLED_PORTLET_PREFIX = 'portlets.obo.';

export function findPortlet(id: string): PortletDescriptor | undefined {
  return portletRegistry.find((portlet) => portlet.id === id);
}

export function isDisabledPortlet(id: string): boolean {
  return id.startsWith(DISABLED_PORTLET_PREFIX);
}
